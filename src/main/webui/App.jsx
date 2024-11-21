import React, { useState } from 'react';
import './App.css';

const App = () => {
  const [username, setUsername] = useState("");  // เก็บค่า username
  const [password, setPassword] = useState("");  // เก็บค่า password
  const [token, setToken] = useState("");  // เก็บ JWT token
  const [data, setData] = useState(null);   // เก็บข้อมูลที่ดึงจาก backend
  const [error, setError] = useState("");  // เก็บข้อผิดพลาดหากมี
  const [isTokenVisible, setIsTokenVisible] = useState(false); // สำหรับการแสดง/ซ่อน token

  // ฟังก์ชันดึงข้อมูลที่ป้องกัน (protected)
  const fetchProtectedData = (token) => {
    fetch('http://localhost:8080/protected', {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    })
      .then(response => {
        if (response.status === 401) {
          throw new Error('Unauthorized: Invalid token');
        }
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => setData(data))
      .catch(error => setError(error.message));
  };

  // ฟังก์ชัน login
  const handleLogin = () => {
    if (!username || !password) {
      setError("Username and password are required.");
      return;
    }

    fetch('http://localhost:8080/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
      .then(response => response.json())
      .then(data => {
        if (data.token) {
          setToken(data.token);
          fetchProtectedData(data.token);  // ดึงข้อมูล protected
        } else {
          setError("Login failed. Please check your credentials.");
        }
      })
      .catch(error => setError(error.message));  // เก็บข้อผิดพลาด
  };

  // ฟังก์ชัน toggle การแสดงผลของ token
  const toggleTokenVisibility = () => {
    setIsTokenVisible(!isTokenVisible);
  };

  const downloadTokenAsFile = () => {
    if (!token) {
      setError("Token is not available.");
      return;
    }
    const blob = new Blob([token], { type: 'text/plain' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'token.txt';
    link.click();
    URL.revokeObjectURL(link.href);
  };


  return (
    <div className="app-container">
      <div className="login-form">
        <h1>Login</h1>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Enter username"
          className="input-field"
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Enter password"
          className="input-field"
        />
        <button onClick={handleLogin} className="btn-submit">Login</button>
        {error && <p className="error-message">{error}</p>}
      </div>

      <div className="protected-data">
        <h2>Protected Data</h2>
        {data ? (
          <pre>{JSON.stringify(data, null, 2)}</pre>
        ) : (
          <p>No protected data available</p>
        )}

        {token && (
          <div>
            <button onClick={downloadTokenAsFile} className="btn-submit">
              Download Token
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;
