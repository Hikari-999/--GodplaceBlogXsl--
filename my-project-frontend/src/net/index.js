import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
// 引入 user_store
import { useUserStore } from '@/stores/user_store';

const authItemName = "authorize"

const defaultFailure = (message , code , url) => {
    console.warn(`请求地址: ${url},状态码:${code},错误信息:${message}`)
    ElMessage.warning(message)
}


const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

const defaultError = (err) => {
    console.error(err)
    ElMessage.warning('发生了错误')
}
//存放用户的token
function storeAccessToken(remember, token, expire){
    const authObj = {
        token: token,
        expire: expire
    }
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}
//拿到用户的token
function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str)
    if(new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj.token
}

//过期了则删除token
function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

function internalPost(url, data, headers, success, failure, error = defaultError){
    axios.post(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}
function internalGet(url, headers, success, failure, error = defaultError){
    axios.get(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        }else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}
function login(username, password, remember, success, failure = defaultFailure){
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(remember, data.token, data.expire)
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    }, failure)
}

function logout(success, failure = defaultFailure){
    get('/api/auth/logout', () => {
        deleteAccessToken()
        // 获取用户 store 实例
        const userStore = useUserStore();
        userStore.clearUser()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, (message, code, url) => {
        // 即使后端返回错误，也清除本地token（解决服务器重启后无法登出的问题）
        deleteAccessToken()
        const userStore = useUserStore();
        userStore.clearUser()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    })
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader() , success, failure)
}


function internalDelete(url, headers, success, failure, error = defaultError){
    axios.delete(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        }else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function del(url, success, failure = defaultFailure) {
    internalDelete(url, accessHeader(), success, failure)
}
//判断是否
function unauthorized() {
    return !takeAccessToken()
}

export {login, get, logout, post, del, unauthorized}