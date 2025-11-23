<script setup>
import { ref } from 'vue'

const activePayment = ref('wechat')
const isAnimating = ref(false)
const donationAmount = ref('')

const selectPayment = (method) => {
  isAnimating.value = true
  activePayment.value = method
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
}

const selectDefaultAmount = (amount) => {
  donationAmount.value = amount
}
</script>

<template>
  <div class="donation-card">
    <div class="card-header">
      <h2>生电（集成平台支付接口需注册企业，遂止）</h2>
      <p>如果您喜欢小破站,不妨为服务器多续租一天</p>
      <p>您的支持是我持续创作的动力</p>
    </div>

    <div class="payment-options">
      <div
          class="payment-option"
          :class="{ active: activePayment === 'wechat', animating: isAnimating }"
          @click="selectPayment('wechat')"
      >
        <div class="payment-icon wechat">
          <!-- 替换为图片 -->
          <img src="@/img/pay/wechatPay.png" alt="微信支付" />
        </div>
        <span>微信支付</span>
      </div>

      <div
          class="payment-option"
          :class="{ active: activePayment === 'alipay', animating: isAnimating }"
          @click="selectPayment('alipay')"
      >
        <div class="payment-icon alipay">
          <!-- 替换为图片 -->
          <img src="@/img/pay/alipay.png" alt="支付宝" />
        </div>
        <span>支付宝</span>
      </div>
    </div>

    <div class="default-amounts">
      <h3>快速选择</h3>
      <div class="amount-options">
        <div class="amount-option" @click="selectDefaultAmount('1')">
          <span class="amount">1</span>
          <span class="description">一个子</span>
        </div>
        <div class="amount-option" @click="selectDefaultAmount('14')">
          <span class="amount">14</span>
          <span class="description">一天服务器</span>
        </div>
        <div class="amount-option" @click="selectDefaultAmount('1400')">
          <span class="amount">1400</span>
          <span class="description">二两碎银</span>
        </div>
      </div>
    </div>

    <div class="donation-amount">
      <div class="amount-input">
        <span class="currency">¥</span>
        <input type="number" placeholder="请输入金额" min="0.01" step="0.01">
      </div>
    </div>

    <div class="donation-button">
      <button>烙铁的路是越走越宽了</button>
    </div>

    <div class="card-animation">
      <div class="wave"></div>
      <div class="bubble bubble-1"></div>
      <div class="bubble bubble-2"></div>
      <div class="bubble bubble-3"></div>
    </div>
  </div>
</template>

<style scoped>
.payment-icon img {
  width: 60px;
  height: 60px;
  border-radius: 50%; /* 设置为圆形 */
  object-fit: cover; /* 保证图片覆盖整个区域 */
}

.default-amounts {
  margin-bottom: 20px;
}

.default-amounts h3 {
  text-align: center;
  margin-bottom: 15px;
  font-size: 16px;
  color: #0277bd;
}

.amount-options {
  display: flex;
  justify-content: space-around;
}

.amount-option {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 30%;
  backdrop-filter: blur(5px);
}

.amount-option:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-3px);
}

.amount-option .amount {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #0288d1;
}

.amount-option .description {
  font-size: 12px;
  color: #01579b;
  opacity: 0.8;
}

.donation-card {
  position: relative;
  max-width: 800px;
  margin: 20px auto;
  padding: 30px 20px;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 150, 180, 0.2);
  overflow: hidden;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  color: #01579b;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 600;
  color: #0288d1;
}

.card-header p {
  margin: 0;
  font-size: 14px;
  color: #0277bd;
  opacity: 0.9;
}

.payment-options {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
}

.payment-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 15px;
  border-radius: 15px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
  width: 45%;
  backdrop-filter: blur(5px);
}

.payment-option.active {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 5px 15px rgba(0, 150, 180, 0.3);
  transform: translateY(-5px);
}

.payment-option.animating {
  animation: pulse 0.3s ease;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.payment-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.payment-icon.wechat {
  background: #07c160;
}

.payment-icon.alipay {
  background: #1677ff;
}

.payment-option span {
  font-size: 14px;
  font-weight: 500;
  color: #01579b;
}

.donation-amount {
  margin-bottom: 30px;
}

.amount-input {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 5px 15px;
  backdrop-filter: blur(5px);
}

.currency {
  font-size: 18px;
  font-weight: 600;
  color: #0288d1;
  margin-right: 5px;
}

.amount-input input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  font-size: 18px;
  color: #01579b;
  outline: none;
}

.amount-input input::placeholder {
  color: #81d4fa;
}

.donation-button button {
  width: 100%;
  padding: 15px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #0288d1 0%, #01579b 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(1, 87, 155, 0.4);
}

.donation-button button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(1, 87, 155, 0.6);
}

.donation-button button:active {
  transform: translateY(0);
}

.card-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100px;
  background: url('data:image/svg+xml;utf8,<svg viewBox="0 0 1200 120" xmlns="http://www.w3.org/2000/svg" fill="rgba(129, 212, 250, 0.3)"><path d="M0,0V46.5c47.7,42.8,122.8,52.8,200,52.8c82.3,0,162.3-12.1,229.7-52.8c64.7-39,112.3-54,172.3-54c68.3,0,126.7,20.8,190,54c66.7,34.8,125.3,52.8,190,52.8c77.3,0,152.3-10,200-52.8V0H0z"></path></svg>');
  background-size: 50% 100px;
  animation: wave 10s linear infinite;
  opacity: 0.6;
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(129, 212, 250, 0.3);
  animation: float 8s infinite ease-in-out;
}

.bubble-1 {
  width: 20px;
  height: 20px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.bubble-2 {
  width: 15px;
  height: 15px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.bubble-3 {
  width: 10px;
  height: 10px;
  top: 40%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-20px) translateX(10px);
    opacity: 0.8;
  }
  100% {
    transform: translateY(0) translateX(0);
    opacity: 0.5;
  }
}

@media (max-width: 480px) {
  .donation-card {
    margin: 10px;
    padding: 20px 15px;
  }

  .payment-options {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }

  .payment-option {
    width: 100%;
  }
}
</style>
