// src/config/websocket.config.ts
export class WebSocketConfig {

    //本地请求
    private static readonly WS_HOST_LOCAL = 'localhost';
    private static readonly WS_PORT = 8080;
    private static readonly WS_ONLINE_PATH = '/ws/online';
    private static readonly WS_AI_PATH = '/api/ws/ai';

    // 根据当前协议确定使用ws还是wss
    public static getWebSocketUrl(): string {
        const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
        return `${protocol}${this.WS_HOST_LOCAL}:${this.WS_PORT}${this.WS_ONLINE_PATH}`;
    }

    // 获取AI聊天WebSocket URL
    public static getAiWebSocketUrl(): string {
        const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
        return `${protocol}${this.WS_HOST_LOCAL}:${this.WS_PORT}${this.WS_AI_PATH}`;
    }

    // 可以添加其他WebSocket相关配置
    public static readonly RECONNECT_INTERVAL = 5000; // 重连间隔(毫秒)
    public static readonly HEARTBEAT_INTERVAL = 30000; // 心跳间隔(毫秒)
}
