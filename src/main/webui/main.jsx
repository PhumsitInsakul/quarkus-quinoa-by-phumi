import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';  // นำเข้า App.jsx ที่เราสร้างไว้

// ใช้ ReactDOM เพื่อ render component 'App' ใน div ที่มี id="app"
ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('app')  // แสดงผลใน #app element
);
