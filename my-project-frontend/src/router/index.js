import { createRouter, createWebHistory } from 'vue-router'
import {unauthorized} from "@/net/index.js";
import { useUserStore } from '@/stores/user_store';
import BlogListView from "@/views/components/card/BlogListView.vue"; // 添加这个导入

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            redirect: '/index',
            children: [
                {
                    path: 'login',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
        },
        // 修改管理路由配置
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/views/components/AdminView.vue'),
            beforeEnter: async (to, from, next) => {
                // 创建一个临时的 Vue 应用来访问 store
                const { createApp } = await import('vue');
                const { useUserStore } = await import('@/stores/user_store');

                // 创建一个临时应用实例以初始化 Pinia
                const app = createApp({});
                // 这里假设你已经在 main.js 中正确设置了 Pinia
                const userStore = useUserStore();

                if (userStore.role === 'admin') {
                    next();
                } else {
                    next('/index');
                }
            }
        }, {
            path: '/blogs',
            name: 'BlogList',
            component: BlogListView
        }, {
            path: '/blog/:id',
            name: 'BlogDetail',
            component: () => import('@/views/components/card/ArticleDetail.vue'), // 指向通用组件
            props: true
        }, {
            path: '/message',
            name: 'MessageCloud',
            component: () => import('@/views/components/card/WordCloud.vue')
        },
        // 添加友链页面路由
        {
            path: '/links',
            name: 'Links',
            component: () => import('@/views/components/card/Links.vue')
        },
        // 添加随笔页面路由
        {
            path: '/talks',
            name: 'Talks',
            component: () => import('@/views/components/card/Talks.vue')
        },
        // 添加简历页面路由
        {
            path: '/job',
            name: 'Job',
            component: () => import('@/views/components/card/ResumeBuilder.vue')
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    if(to.name.startsWith('welcome') && !isUnauthorized) {
        next('/index')
    }
    else {
        next()
    }
})

export default router
