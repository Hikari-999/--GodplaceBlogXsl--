<template>
  <div class="acknowledgements-card">
    <div class="header">
      <h2>鸣谢</h2>
      <p>感谢以下开源项目和工具的支持</p>
    </div>

    <div class="content">
      <el-collapse v-model="activeNames">
        <el-collapse-item title="前端技术栈" name="1">
          <div class="tech-stack">
            <el-tag v-for="tech in frontendTechs" :key="tech.name"
                    :type="tech.type" class="tech-tag">
              {{ tech.name }}
            </el-tag>
          </div>
          <p class="description">前端基于 Vue3 和 Element Plus 构建，采用现代化的开发技术栈</p>
        </el-collapse-item>

        <el-collapse-item title="后端技术栈" name="2">
          <div class="tech-stack">
            <el-tag v-for="tech in backendTechs" :key="tech.name"
                    :type="tech.type" class="tech-tag">
              {{ tech.name }}
            </el-tag>
          </div>
          <p class="description">后端基于 Spring Boot 构建，集成多种常用技术组件</p>
        </el-collapse-item>

        <el-collapse-item title="技术介绍" name="3">
          <ul class="projects-list">
            <li v-for="project in openSourceProjects" :key="project.name">
              <a :href="project.url" target="_blank" class="project-link">
                {{ project.name }}
              </a>
              <span class="project-desc"> - {{ project.description }}</span>
            </li>
          </ul>
        </el-collapse-item>

        <el-collapse-item title="特别感谢" name="4">
          <div class="special-thanks">
            <p class="reference-title">后端框架参考:青空の霞光</p>
            <p class="reference-url">https://github.com/itbaima-study/SpringBoot-Vue-Template-Jwt</p>

            <p class="reference-title">前端页面设计风格参考:小张的个人博客</p>
            <p class="reference-url">https://gitee.com/mrzym/stable-version-of-blog</p>

            <p class="reference-title">前端部分功能实现参考:YShuJia/UTOPIA</p>
            <p class="reference-url">https://gitee.com/yshujia-utopia/UTOPIA</p>

            <p class="reference-title">前端UI设计参考:kuailemao/Ruyu-Blog</p>
            <p class="reference-url">https://gitee.com/kuailemao/ruyu-blog</p>

            <p>感谢使用本博客系统的每一位用户</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div class="footer">
      <p>本项目遵循 EPL-2.0 开源协议</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeNames = ref(['1', '2', '3', '4'])

const frontendTechs = ref([
  { name: 'Vue3', type: '' },
  { name: 'Element Plus', type: 'success' },
  { name: 'Vite', type: 'warning' },
  { name: 'Vue Router', type: '' },
  { name: 'Pinia', type: 'success' }
])

const backendTechs = ref([
  { name: 'Spring Boot', type: '' },
  { name: 'MySQL', type: 'success' },
  { name: 'Redis', type: 'warning' },
  { name: 'RabbitMQ', type: '' },
  { name: 'MyBatisPlus', type: 'success' }
])

const openSourceProjects = ref([
  {
    name: 'Vue.js',
    url: 'https://vuejs.org/',
    description: '渐进式JavaScript框架'
  },
  {
    name: 'Element Plus',
    url: 'https://element-plus.org/',
    description: '基于 Vue 3 的组件库'
  },
  {
    name: 'Spring Boot',
    url: 'https://spring.io/projects/spring-boot',
    description: '简化Spring应用的初始搭建以及开发过程'
  },
  {
    name: 'Redis',
    url: 'https://redis.io/',
    description: '内存数据结构存储系统'
  }
])
</script>

<style scoped>
.acknowledgements-card {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: white;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.header p {
  font-size: 16px;
  opacity: 0.9;
}

.content {
  margin-bottom: 30px;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.tech-tag {
  margin: 5px;
}

.description {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 10px;
}

.projects-list {
  padding-left: 20px;
}

.projects-list li {
  margin-bottom: 10px;
  line-height: 1.5;
}

.project-link {
  color: #409eff;
  text-decoration: none;
  font-weight: bold;
}

.project-link:hover {
  text-decoration: underline;
}

.project-desc {
  color: #e0e0e0;
}

.special-thanks p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 14px;
  opacity: 0.8;
}

:deep(.el-collapse) {
  border: none;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-collapse-item__header) {
  background-color: rgba(255, 255, 255, 0.15);
  color: white;
  border: none;
  padding-left: 20px;
}

:deep(.el-collapse-item__wrap) {
  background-color: rgba(255, 255, 255, 0.05);
  border: none;
}

:deep(.el-collapse-item__content) {
  color: white;
  padding: 20px;
}

.special-thanks {
  padding: 15px 0;
}

.special-thanks p {
  margin-bottom: 15px;
  line-height: 1.6;
  position: relative;
  padding-left: 20px;
}

.special-thanks a {
  color: #409eff;
  text-decoration: none;
  transition: all 0.3s ease;
  word-break: break-all;
}

.special-thanks a:hover {
  color: #ff6b6b;
  text-decoration: underline;
}

.special-thanks .reference-title {
  font-weight: bold;
  color: #ffd166;
  margin-top: 20px;
  border-left: 3px solid #ffd166;
  padding-left: 15px;
}

.special-thanks .reference-url {
  font-size: 13px;
  color: #a9d1ff;
  font-family: 'Courier New', monospace;
  background: rgba(0, 0, 0, 0.2);
  padding: 8px 12px;
  border-radius: 4px;
  margin: 5px 0 15px 0;
  word-break: break-all;
}

.footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 14px;
  opacity: 0.8;
}
</style>
