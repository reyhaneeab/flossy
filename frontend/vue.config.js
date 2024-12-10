const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // FRONTEND PORT
    port: 8081,
    proxy: {
      "/api": {
        target: process.env.VUE_APP_APIGATEWAY_URL || 'http://localhost:8084', 
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
});

