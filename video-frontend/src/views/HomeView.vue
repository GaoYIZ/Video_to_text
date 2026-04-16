<template>
  <div class="home-view">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="28" color="#1890ff"><VideoCamera /></el-icon>
          <span>AI语音转写</span>
        </div>
        <nav class="nav-menu">
          <el-button text @click="$router.push('/')" :class="{active: $route.path === '/'}">首页</el-button>
          <el-button text @click="$router.push('/history')" :class="{active: $route.path === '/history'}">历史记录</el-button>
        </nav>
      </div>
    </header>

    <!-- Banner区域 -->
    <section class="banner">
      <div class="banner-content">
        <h1>AI 智能语音转文字</h1>
        <p class="subtitle">支持视频/音频文件上传、链接解析,自动生成智能总结</p>
        <div class="features">
          <div class="feature-item">
            <el-icon :size="24"><CircleCheck /></el-icon>
            <span>准确率高达 98%</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><CircleCheck /></el-icon>
            <span>支持多种格式</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><CircleCheck /></el-icon>
            <span>AI 智能总结</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能区 -->
    <section class="main-section">
      <div class="container">
        <h2 class="section-title">选择转写方式</h2>
        
        <div class="upload-cards">
          <!-- 卡片1: 机器快转 -->
          <el-card class="upload-card" shadow="hover" @click="$router.push('/upload-audio')">
            <div class="card-badge">推荐</div>
            <div class="card-icon-wrapper">
              <el-icon :size="56" color="#1890ff"><Headset /></el-icon>
            </div>
            <h3>上传音频</h3>
            <p class="card-desc">直接上传音频文件,快速转写</p>
            <ul class="card-features">
              <li><el-icon><Check /></el-icon> 支持 mp3/wav/m4a 等格式</li>
              <li><el-icon><Check /></el-icon> 转写速度快 3-5 倍</li>
              <li><el-icon><Check /></el-icon> 节省流量和时间</li>
            </ul>
            <el-button type="primary" size="large" class="card-btn">
              立即上传
            </el-button>
          </el-card>

          <!-- 卡片2: 上传视频 -->
          <el-card class="upload-card" shadow="hover" @click="$router.push('/upload-video')">
            <div class="card-icon-wrapper">
              <el-icon :size="56" color="#52c41a"><VideoCamera /></el-icon>
            </div>
            <h3>上传视频</h3>
            <p class="card-desc">上传视频文件,自动提取音频</p>
            <ul class="card-features">
              <li><el-icon><Check /></el-icon> 支持 mp4/avi/mov 等格式</li>
              <li><el-icon><Check /></el-icon> 自动转换音频</li>
              <li><el-icon><Check /></el-icon> 保留原始视频</li>
            </ul>
            <el-button type="success" size="large" class="card-btn">
              立即上传
            </el-button>
          </el-card>

          <!-- 卡片3: 链接解析 -->
          <el-card class="upload-card" shadow="hover" @click="$router.push('/parse-url')">
            <div class="card-icon-wrapper">
              <el-icon :size="56" color="#faad14"><Link /></el-icon>
            </div>
            <h3>解析链接</h3>
            <p class="card-desc">粘贴视频链接,自动下载转写</p>
            <ul class="card-features">
              <li><el-icon><Check /></el-icon> 支持 B站/YouTube 等</li>
              <li><el-icon><Check /></el-icon> 无需手动下载</li>
              <li><el-icon><Check /></el-icon> 移动端友好</li>
            </ul>
            <el-button type="warning" size="large" class="card-btn">
              立即解析
            </el-button>
          </el-card>
        </div>
      </div>
    </section>

    <!-- 使用流程 -->
    <section class="process-section">
      <div class="container">
        <h2 class="section-title">使用流程</h2>
        <div class="process-steps">
          <div class="step-item">
            <div class="step-number">1</div>
            <h4>上传文件</h4>
            <p>选择音频/视频文件或粘贴链接</p>
          </div>
          <div class="step-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <h4>AI 转写</h4>
            <p>智能识别语音并转为文字</p>
          </div>
          <div class="step-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <h4>生成总结</h4>
            <p>AI 自动提炼核心要点</p>
          </div>
          <div class="step-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div class="step-item">
            <div class="step-number">4</div>
            <h4>查看结果</h4>
            <p>在线阅读或下载文本</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <p>© 2026 AI语音转写系统 | 基于 Whisper + LangChain + OpenAI</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 头部 */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  cursor: pointer;
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-menu .el-button {
  color: #595959;
}

.nav-menu .el-button.active {
  color: #1890ff;
  font-weight: 500;
}

/* Banner */
.banner {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  padding: 80px 24px;
  text-align: center;
}

.banner-content {
  max-width: 800px;
  margin: 0 auto;
}

.banner h1 {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px;
}

.subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 32px;
}

.features {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

/* 主内容区 */
.main-section {
  padding: 60px 24px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 48px;
}

.upload-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.upload-card {
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  padding: 32px 24px;
  text-align: center;
}

.upload-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-badge {
  position: absolute;
  top: -1px;
  right: 24px;
  background: #ff4d4f;
  color: #fff;
  padding: 4px 12px;
  border-radius: 0 0 4px 4px;
  font-size: 12px;
  font-weight: 500;
}

.card-icon-wrapper {
  margin-bottom: 16px;
}

.upload-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
}

.card-desc {
  color: #8c8c8c;
  font-size: 14px;
  margin: 0 0 20px;
}

.card-features {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
  text-align: left;
}

.card-features li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #595959;
  font-size: 14px;
}

.card-features .el-icon {
  color: #52c41a;
  font-size: 16px;
}

.card-btn {
  width: 100%;
}

/* 流程区 */
.process-section {
  background: #fff;
  padding: 60px 24px;
}

.process-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.step-item {
  text-align: center;
  flex: 1;
  min-width: 150px;
  max-width: 200px;
}

.step-number {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  margin: 0 auto 16px;
}

.step-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
}

.step-item p {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.step-arrow {
  color: #d9d9d9;
  font-size: 24px;
}

/* 底部 */
.footer {
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 24px;
  text-align: center;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .banner {
    padding: 48px 16px;
  }

  .banner h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 15px;
  }

  .features {
    gap: 16px;
  }

  .main-section,
  .process-section {
    padding: 40px 16px;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 32px;
  }

  .upload-cards {
    grid-template-columns: 1fr;
  }

  .process-steps {
    flex-direction: column;
  }

  .step-arrow {
    transform: rotate(90deg);
  }
}
</style>
