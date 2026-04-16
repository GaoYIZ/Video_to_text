<template>
  <div class="home-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>AI 视频助手</h1>
      <p>智能语音转写 · AI 内容总结 · 多格式支持</p>
    </div>

    <!-- 核心功能金刚区 -->
    <section class="function-cards">
      <!-- 卡片1: 提取链接视频 -->
      <el-card class="func-card link-card" shadow="hover" @click="showLinkDialog = true">
        <div class="card-icon">
          <el-icon :size="48" color="#1890ff"><Link /></el-icon>
        </div>
        <h3>提取链接视频</h3>
        <p>支持 YouTube/B站/抖音等平台</p>
        <el-tag size="small" effect="plain">在线解析</el-tag>
      </el-card>

      <!-- 卡片2: 提取本地视频 (核心) -->
      <el-card class="func-card video-card featured" shadow="hover" @click="triggerFileInput('video')">
        <div class="card-badge">推荐</div>
        <div class="card-icon">
          <el-icon :size="56" color="#fff"><VideoCamera /></el-icon>
        </div>
        <h3>提取本地视频</h3>
        <p>上传 MP4/MOV/AVI 等格式</p>
        <el-tag size="small" type="danger" effect="dark">核心功能</el-tag>
      </el-card>

      <!-- 卡片3: 提取音频文件 -->
      <el-card class="func-card audio-card" shadow="hover" @click="triggerFileInput('audio')">
        <div class="card-icon">
          <el-icon :size="48" color="#13c2c2"><Headset /></el-icon>
        </div>
        <h3>提取音频文件</h3>
        <p>支持 MP3/WAV/M4A 等格式</p>
        <el-tag size="small" type="success" effect="plain">快速转写</el-tag>
      </el-card>
    </section>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".mp4,.mov,.avi,.wmv,.flv,.mkv"
      style="display: none"
      @change="handleVideoUpload"
    />
    <input
      ref="audioInputRef"
      type="file"
      accept=".mp3,.wav,.m4a,.aac,.flac,.wma"
      style="display: none"
      @change="handleAudioUpload"
    />

    <!-- 链接解析对话框 -->
    <el-dialog
      v-model="showLinkDialog"
      title="解析视频链接"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="videoUrl"
        placeholder="粘贴视频链接,如: https://www.bilibili.com/video/BV1xx"
        size="large"
        clearable
      >
        <template #prefix>
          <el-icon><Link /></el-icon>
        </template>
      </el-input>

      <div class="supported-platforms">
        <p>支持的平台:</p>
        <el-space wrap>
          <el-tag effect="plain">Bilibili</el-tag>
          <el-tag effect="plain">YouTube</el-tag>
          <el-tag effect="plain">抖音</el-tag>
          <el-tag effect="plain">快手</el-tag>
        </el-space>
      </div>

      <template #footer>
        <el-button @click="showLinkDialog = false">取消</el-button>
        <el-button type="primary" :loading="parsing" @click="handleParseUrl">
          开始解析
        </el-button>
      </template>
    </el-dialog>

    <!-- 最近文件列表 -->
    <section class="recent-files" v-if="recentList.length > 0">
      <div class="section-header">
        <h2>最近文件</h2>
        <el-button text @click="$router.push('/history')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div class="file-grid">
        <el-card 
          v-for="file in recentList" 
          :key="file.id"
          class="file-card"
          shadow="hover"
          @click="viewFile(file.id)"
        >
          <div class="file-status" :class="file.status"></div>
          
          <div class="file-info">
            <h4 class="file-name">{{ file.originalName }}</h4>
            <p class="file-summary">{{ file.summary ? file.summary.substring(0, 60) + '...' : '等待处理...' }}</p>
            
            <div class="file-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(file.createTime) }}
              </span>
              <span class="meta-item">
                <el-icon><Timer /></el-icon>
                {{ file.duration || '--:--' }}
              </span>
            </div>
          </div>

          <div class="file-actions">
            <el-dropdown trigger="click" @click.stop>
              <el-button text size="small">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="viewFile(file.id)">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-dropdown-item>
                  <el-dropdown-item divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { parseVideoUrl, uploadVideoFile, uploadAudioFile, convertToAudio, processWithAi, getHistoryList } from '../api/video'

const router = useRouter()
const showLinkDialog = ref(false)
const videoUrl = ref('')
const parsing = ref(false)
const fileInputRef = ref(null)
const audioInputRef = ref(null)
const recentList = ref([])

// 触发文件选择
const triggerFileInput = (type) => {
  if (type === 'video') {
    fileInputRef.value?.click()
  } else {
    audioInputRef.value?.click()
  }
}

// 处理视频上传
const handleVideoUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    ElMessage.info('开始上传视频...')
    
    // 上传视频
    const uploadRes = await uploadVideoFile(file)
    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message)
    }

    const videoId = uploadRes.data.id
    ElMessage.success('上传成功,开始转换...')

    // 转换音频
    await convertToAudio(videoId)
    ElMessage.success('转换完成,开始 AI 分析...')

    // AI 处理
    await processWithAi(videoId)
    ElMessage.success('处理完成!')

    // 刷新列表
    await fetchRecentFiles()
    
    // 跳转到结果页
    router.push(`/result/${videoId}`)

  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error(error.message || '处理失败')
  } finally {
    event.target.value = ''
  }
}

// 处理音频上传
const handleAudioUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    ElMessage.info('开始上传音频...')
    
    // 上传音频
    const uploadRes = await uploadAudioFile(file)
    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message)
    }

    const videoId = uploadRes.data.id
    ElMessage.success('上传成功,开始 AI 分析...')

    // AI 处理
    await processWithAi(videoId)
    ElMessage.success('处理完成!')

    // 刷新列表
    await fetchRecentFiles()
    
    // 跳转到结果页
    router.push(`/result/${videoId}`)

  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error(error.message || '处理失败')
  } finally {
    event.target.value = ''
  }
}

// 解析链接
const handleParseUrl = async () => {
  if (!videoUrl.value) {
    ElMessage.warning('请输入视频链接')
    return
  }

  parsing.value = true
  try {
    const res = await parseVideoUrl(videoUrl.value)
    if (res.code !== 200) {
      throw new Error(res.message)
    }

    ElMessage.success('解析成功!')
    showLinkDialog.value = false
    videoUrl.value = ''
    
    // TODO: 后续实现完整的链接解析流程
    
  } catch (error) {
    console.error('解析失败:', error)
    ElMessage.error(error.message || '解析失败')
  } finally {
    parsing.value = false
  }
}

// 查看文件
const viewFile = (id) => {
  router.push(`/result/${id}`)
}

// 获取最近文件
const fetchRecentFiles = async () => {
  try {
    const res = await getHistoryList(1, 6)
    if (res.code === 200) {
      recentList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取列表失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchRecentFiles()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 8px;
}

.page-header p {
  font-size: 15px;
  color: #8c8c8c;
  margin: 0;
}

/* 功能卡片 */
.function-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 48px;
}

.func-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.func-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #C084FC 0%, #9333EA 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.card-icon {
  margin-bottom: 16px;
}

.func-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
}

.func-card p {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px;
}

/* 特色卡片 - 紫色渐变 */
.video-card.featured {
  background: linear-gradient(to bottom right, #C084FC, #9333EA);
  color: #fff;
}

.video-card.featured h3,
.video-card.featured p {
  color: #fff;
}

.video-card.featured :deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

/* 最近文件 */
.recent-files {
  margin-top: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.file-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  position: relative;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.file-status {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  z-index: 1;
}

.file-status.completed {
  background: #52c41a;
  box-shadow: 0 0 8px rgba(82, 196, 26, 0.6);
  animation: pulse 2s infinite;
}

.file-status.processing {
  background: #ff4d4f;
  box-shadow: 0 0 8px rgba(255, 77, 79, 0.6);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.file-info {
  padding: 8px 0;
}

.file-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-summary {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.file-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #bfbfbf;
}

.file-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}

.supported-platforms {
  margin-top: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.supported-platforms p {
  margin: 0 0 8px;
  font-size: 13px;
  color: #595959;
}

@media (max-width: 768px) {
  .function-cards {
    grid-template-columns: 1fr;
  }

  .file-grid {
    grid-template-columns: 1fr;
  }
}
</style>
