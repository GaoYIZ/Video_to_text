<template>
  <div class="home-view">
    <!-- 头部 -->
    <header class="header">
      <div class="header-content">
        <h1 class="logo">
          <el-icon :size="32" color="#409EFF"><VideoCamera /></el-icon>
          <span>视频智能总结</span>
        </h1>
        <el-button text @click="$router.push('/history')">
          <el-icon><Clock /></el-icon>
          历史记录
        </el-button>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="feature-cards">
        <!-- 功能卡片1: 解析链接 -->
        <el-card class="feature-card" shadow="hover" @click="$router.push('/parse-url')">
          <div class="card-icon">
            <el-icon :size="48" color="#409EFF"><Link /></el-icon>
          </div>
          <h3>解析视频链接</h3>
          <p>支持 B站、YouTube 等主流平台</p>
          <el-tag type="primary" effect="plain">推荐</el-tag>
        </el-card>

        <!-- 功能卡片2: 上传视频 -->
        <el-card class="feature-card" shadow="hover" @click="$router.push('/upload-video')">
          <div class="card-icon">
            <el-icon :size="48" color="#67C23A"><Upload /></el-icon>
          </div>
          <h3>上传本地视频</h3>
          <p>支持 mp4/avi/mov 等格式</p>
          <el-tag type="success" effect="plain">常用</el-tag>
        </el-card>

        <!-- 功能卡片3: 上传音频 -->
        <el-card class="feature-card" shadow="hover" @click="$router.push('/upload-audio')">
          <div class="card-icon">
            <el-icon :size="48" color="#E6A23C"><Headset /></el-icon>
          </div>
          <h3>上传音频文件</h3>
          <p>直接处理音频,更快更省流量</p>
          <el-tag type="warning" effect="plain">快速</el-tag>
        </el-card>
      </div>

      <!-- 使用说明 -->
      <el-card class="tips-card" shadow="never">
        <template #header>
          <div class="tips-header">
            <el-icon><InfoFilled /></el-icon>
            <span>使用提示</span>
          </div>
        </template>
        <div class="tips-content">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <div class="tip-item">
                <strong>📱 移动端推荐</strong>
                <p>先转换音频再上传,节省流量和时间</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="tip-item">
                <strong>⚡ 处理速度</strong>
                <p>音频文件比视频处理快 3-5 倍</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="tip-item">
                <strong>💾 历史记录</strong>
                <p>所有处理结果自动保存,随时查看</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  margin: 0;
  color: #303133;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.feature-card {
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  padding: 30px 20px;
  border-radius: 12px;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.card-icon {
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 18px;
  margin: 15px 0 10px;
  color: #303133;
}

.feature-card p {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.tips-card {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #303133;
}

.tips-content {
  padding: 10px 0;
}

.tip-item {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.tip-item strong {
  display: block;
  margin-bottom: 5px;
  color: #303133;
}

.tip-item p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header-content {
    padding: 12px 15px;
  }

  .logo {
    font-size: 16px;
  }

  .logo .el-icon {
    font-size: 24px !important;
  }

  .main-content {
    padding: 20px 15px;
  }

  .feature-cards {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .feature-card {
    padding: 20px 15px;
  }
}
</style>
