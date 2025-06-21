import React from "react";
import { useNavigate } from "react-router-dom";
import './Header.css';
const Header = () => {
    const nav = useNavigate();
  return (
    <div>
      <header className="header">
        <h1 className="logo" onClick={()=>{nav('/')}}>지식 관리 시스템</h1>
        <nav className="nav">
          <button className="nav-button" onClick={() => {nav('/employee')}}>직원</button>
          <button className="nav-button" onClick={() => {nav('/document')}}>문서</button>
          <button className="nav-button" onClick={() => {nav('/schedule')}}>일정</button>
          <button className="nav-button" onClick={() => {nav('/task')}}>업무방</button>
          <button className="nav-button" onClick={() => {nav('/education')}}>교육</button>
          <button className="nav-button" onClick={() => {nav('/club')}}>동호회</button>

        </nav>
        <div className="user-menu">
          <button className="user-button">홍길동</button>
          <button className="user-button">로그아웃</button>
        </div>
      </header>
      </div>
  );
}

export default Header;