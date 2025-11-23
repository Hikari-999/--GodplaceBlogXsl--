<template>
  <div class="full-cover">
    <el-container class="main-container">
      <el-main class="main-content">
        <div class="background-container">
          <img class="background-image" src="@/img/imageBackground.jpg" alt="Background Image">
          <div class="wave-effect"></div>
          <!-- 在 template 中 -->

        </div>
        <!-- 在这里添加随机文本展示 -->
        <div class="my-blog">
          欢迎来到Godplace的博客
        </div>
        <div class="random-text">
          {{ displayedText }}
        </div>

        <!-- 新增内容区域 -->
        <MainView />
        <!-- 修改页脚的位置，放在 main-content 内部最后 -->
        <footer class="page-footer">
          <div class="footer-content">
            <p>&copy; 2025 Godplace Blog.</p>
            <p>网站备案号: 蜀ICP备2025159750号 | 联系邮箱: 1010287405@qq.com</p>
          </div>
        </footer>
      </el-main>
    </el-container>

    <div class="arrow-down" v-show="showArrow" @click="scrollDown">
      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
        <path d="M16 24l-11-8 2-2 9 9 9-9 2 2z" fill="#fff"/>
      </svg>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onUnmounted } from "vue";
import MainView from "@/views/components/MainView.vue";
import MusicPlayer from "@/views/components/music/MusicPlayer.vue";
// 引入 user_store
import { useUserStore } from '@/stores/user_store';

// 获取用户 store 实例
const userStore = useUserStore();

// 添加响应式数据来控制导航栏的显示状态
const lastScrollTop = ref(0);
const headerHeight = ref(0);
const scrollThreshold = ref(0);
// 添加控制箭头显示的响应式数据
const showArrow = ref(true);
// 监听滚动事件
const handleScroll = () => {
  const scrollElement = document.querySelector('.main-content');
  if (!scrollElement) return;

  const scrollTop = scrollElement.scrollTop;

  // 控制箭头显示/隐藏
  showArrow.value = scrollTop < 100; // 当滚动超过100px时隐藏箭头

  // 只有滚动距离超过阈值才触发
  if (Math.abs(scrollTop - lastScrollTop.value) <= scrollThreshold.value) {
    lastScrollTop.value = scrollTop;
    return;
  }

  // 向下滚动且滚动距离大于导航栏高度时隐藏
  if (scrollTop > lastScrollTop.value && scrollTop > headerHeight.value) {
    // 通过事件通知 App.vue 隐藏导航栏
    window.dispatchEvent(new CustomEvent('headerVisibilityChange', { detail: false }));
  }
  // 向上滚动或滚动到顶部附近时显示
  else if (scrollTop < lastScrollTop.value || scrollTop < headerHeight.value) {
    // 通过事件通知 App.vue 显示导航栏
    window.dispatchEvent(new CustomEvent('headerVisibilityChange', { detail: true }));
  }

  lastScrollTop.value = scrollTop;
};

// 修改 scrollDown 函数
const scrollDown = () => {
  console.log('Arrow clicked'); // 调试用
  const scrollElement = document.querySelector('.main-content');
  if (scrollElement) {
    // 滚动到刚好超过首屏的位置
    scrollElement.scrollTo({
      top: window.innerHeight + 50, // 滚动到首屏下方50px位置
      behavior: 'smooth'
    });
  }
};

// 随机文本数组
const startUsingTexts = computed(() => [
  "本网站致力于打造漫画风个人博客",
  "不需要前世今生的铺垫，不需要生死离别的确信，世间最动人的情愫，莫过于，怦然心动。",
  "那些生死悲欢.你以为是人世一生.可月亮见过太多.知道那只是沉埃一瞬.",
  "美丽的花季却没有一场朦胧的爱恋的确遗憾。\n" +
  "但又何必为了不是两情相悦的寂寞去埋葬更美好的明天？",
  "拥抱太温柔 像旧时候 像老朋友",
  "难得白发上眉梢 却叫相思到词穷",
  "那么想喝我泡的茶吗？当初在波士顿，你可是全倒掉了。",
  "十里琵琶 悲吟陌上花 舒红袖血色染云霞 飞花散天涯 明心无瑕 长剑映风华 纵红尘不过虚话 此身付虚话 尽此生韶华 终付一盏清茶 ",
  "我闭上眼睛触摸星空，阅读宇宙留给我的盲文",
  "时光只解催人老，不信多情，长恨离亭，泪滴春衫酒易醒。",
  "梧桐昨夜西风急，淡月胧明，好梦频惊，何处高楼雁一声。",
  "住楼就住顶层，居高且能望远。看戏就坐后排，看不清戏却看得清看戏的人。",
  "林间松韵，石上泉声，静里听来，识天地自然鸣珮；草际烟光，水心云影，闲中观去，见乾坤最上文章。",
  "被梦想俘虏的人就是在追逐自己的噩运。满地都是六便士，他却抬头看见了月亮。",
  "您心目中的黄金时代，就好比大马士革和君士坦丁堡，远远望去何等壮丽；待您走进街巷，才见到有癞皮的叫花子和倒毙的死狗。",
  "快乐人有个不怎么有出息的前提，不太在乎别人的看法，也不打算立志对世界有什么贡献。",
  "我们的习惯、我们的恐惧，决定了我们的选择。我们是自身推导的结果。",
  "大多数人过着一种平静的绝望生活，他们心中的歌和他们一起埋入坟墓。",
  "生命如此短暂，我们没有时间去互相争吵，道歉，发泄，责备，时间只够用来去爱，可它又只有一瞬，令人惋惜！",
  "前方仍会有星期日。一条小径分开枝条，从草叶间一划而过。",
  "我去旅行，是因为我决定了要去，并不是因为对风景的兴趣。",
  "当我不想做什么事情的时候，我会抗拒，而当我乐于做什么的时候，什么都阻挡不了我。",
  "生活不能等待别人来安排，要自己去争取和奋斗，而不论其结果是喜是悲，但可以慰藉的是，你总不枉在这世界上活了一场。"
]);

// 当前随机文本
const randomStartText = computed(() => {
  const index = Math.floor(Math.random() * startUsingTexts.value.length);
  return startUsingTexts.value[index];
});

// 当前展示的文本
const displayedText = ref('');
const currentIndex = ref(0);
// 添加一个变量来保存当前正在显示的完整文本
const currentFullText = ref('');

// 控制逐字显示
function showTextStepByStep(fullText: string, index: number = 0) {
  // 如果是新的一段文本，更新currentFullText
  if (index === 0) {
    currentFullText.value = fullText;
  }

  // 确保只显示当前文本，避免被其他调用干扰
  if (fullText !== currentFullText.value) {
    return;
  }

  if (index <= fullText.length) {
    displayedText.value = fullText.substring(0, index);
    setTimeout(() => {
      showTextStepByStep(fullText, index + 1);
    }, 150); // 控制逐字显示速度（毫秒）
  } else {
    // 全部显示后，等待一段时间后切换下一个随机文本
    setTimeout(() => {
      // 获取新的随机文本
      const randomIndex = Math.floor(Math.random() * startUsingTexts.value.length);
      const nextText = startUsingTexts.value[randomIndex];
      // 确保不会连续显示相同的文本（当数组长度大于1时）
      if (nextText === fullText && startUsingTexts.value.length > 1) {
        const newIndex = (randomIndex + 1) % startUsingTexts.value.length;
        showTextStepByStep(startUsingTexts.value[newIndex]);
      } else {
        showTextStepByStep(nextText);
      }
    }, 3000); // 控制显示完整文本后切换下一条的时间
  }
}

onMounted(() => {
  // 初始展示第一条随机文本
  showTextStepByStep(randomStartText.value);

  // 添加滚动事件监听
  const scrollElement = document.querySelector('.main-content');
  if (scrollElement) {
    scrollElement.addEventListener('scroll', handleScroll);
  }
});

onUnmounted(() => {
  // 移除滚动事件监听
  const scrollElement = document.querySelector('.main-content');
  if (scrollElement) {
    scrollElement.removeEventListener('scroll', handleScroll);
  }
});
</script>

<style scoped>
.my-blog {
  position: absolute;
  top: 48%; /* 调整垂直位置 */
  left: 50%; /* 水平居中 */
  transform: translate(-50%, -50%); /* 精确居中 */
  color: #ffffff; /* 白色字体 */
  font-size: 48px; /* 增大字体 */
  font-weight: bold;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif; /* 更换为动漫风格字体 */
  text-align: center;
  padding: 15px 30px;
  border-radius: 12px;
  z-index: 3;
  animation: glow 2s ease-in-out infinite alternate, float 6s ease-in-out infinite; /* 添加浮动和光晕动画 */
}



/* 暗黑模式样式 */
html.dark .my-blog {
  color: #e0e0e0;
}

html.dark .random-text {
  color: #e0e0e0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

/* 暗黑模式下的背景图片调整 */
html.dark .background-image {
  opacity: 0.6;
  filter: brightness(1);
}

html.dark .arrow-down svg {
  fill: #e0e0e0;
}

/* 暗黑模式下的页脚 */
html.dark .page-footer {
  background-color: rgba(30, 30, 30, 0.9);
  color: #e0e0e0;
}

/* 滚动条暗黑模式样式 */
html.dark .main-content::-webkit-scrollbar-track {
  background-color: #2d2d2d;
}

html.dark .main-content::-webkit-scrollbar-thumb {
  background-color: rgba(100, 100, 100, 0.5);
}

html.dark .main-content::-webkit-scrollbar-thumb:hover {
  background-color: rgba(150, 150, 150, 0.8);
}




/* 增强光晕效果 */
@keyframes glow {
  from {
    text-shadow: 0 0 10px rgba(255, 0, 255, 0.6), 0 0 20px rgba(255, 0, 255, 0.4);
  }
  to {
    text-shadow: 0 0 20px rgba(255, 0, 255, 0.9), 0 0 30px rgba(255, 0, 255, 0.7);
  }
}

.random-text {
  position: absolute;
  top: 57%; /* 垂直居中 */
  left: 50%; /* 水平居中 */
  transform: translate(-50%, -50%); /* 精确居中 */
  color: #fff; /* 字体颜色为白色 */
  font-size: 24px; /* 字体大小 */
  font-weight: bold; /* 加粗 */
  text-align: center;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5); /* 可选：添加文字阴影，提高可读性 */
  z-index: 2;
}

.full-cover {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  position: relative;
}

.main-container {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  position: relative;
}

.main-content {
  height: 100%; /* 改为 100%，让内容撑满整个容器 */
  position: relative;
  overflow-y: auto; /* 允许垂直滚动 */
  padding-top: 100vh; /* 避免内容被背景图遮挡 */
}

/* 定义滚动条轨道 */
.main-content::-webkit-scrollbar-track {
  background-color: transparent; /* 轨道背景颜色 */
  border-radius: 10px; /* 轨道圆角 */
}

/* 定义滚动条滑块 */
.main-content::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3); /* 滑块背景颜色 */
  border-radius: 10px; /* 滑块圆角 */
}

/* 定义滚动条宽度 */
.main-content::-webkit-scrollbar {
  width: 10px; /* 滚动条宽度 */
}

/* 滑块悬停时的样式 */
.main-content::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.6); /* 悬停时滑块背景颜色 */
}

.background-container {
  width: 100%;
  height: 100vh; /* 覆盖整个视口高度 */
  position: fixed; /* 固定定位，确保覆盖整个视口 */
  top: 0;
  left: 0;
  z-index: -1; /* 确保在所有内容之下 */
}

.additional-content {
  position: relative;
  z-index: 2;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.1); /* 背景透明度调整 */
  color: white;
  font-size: 18px;
}

.background-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  z-index: -2; /* 确保在 background-container 之下 */
}

/* 修改 arrow-down 样式 */
.arrow-down {
  position: fixed; /* 改为 fixed 定位 */
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  animation: bounce 2s infinite;
  cursor: pointer;
  z-index: 10; /* 大幅提高 z-index 值 */
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.arrow-down svg {
  fill: #fff;
  width: 32px;
  height: 32px;
  pointer-events: none; /* 防止svg拦截点击事件 */
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

/* 修改页脚样式，让它自然显示在底部 */
.page-footer {
  background-color: rgba(0, 0, 0, 0.8);
  color: #fff;
  text-align: center;
  padding: 20px;
  margin-left: -20px;
  margin-right: -20px;
  font-size: 14px;
}

.footer-content p {
  margin: 5px 0;
}

.footer-content a {
  color: #409eff;
  text-decoration: none;
}

.footer-content a:hover {
  text-decoration: underline;
}
</style>
