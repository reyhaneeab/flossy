const { defineConfig } = require('@vue/cli-service')


module.exports = defineConfig({
  transpileDependencies: true
})
module.exports = {
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8085", // Your backend service
        changeOrigin: true,
      },
    },
  },
};

