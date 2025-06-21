import React from "react";
import { useNavigate } from "react-router-dom";
import './Header.css';
import { useContext } from "react";
import { UserContext } from "../Context/UserContext";
const Header = () => {
    const { loggedInUser, setLoggedInUser, dummyUsers } = useContext(UserContext);
    console.log(loggedInUser);
    const nav = useNavigate();

    const toggleRole = () => {
    const nextUser =
      loggedInUser.role === 'EMPLOYEE'
        ? dummyUsers.admin
        : dummyUsers.employee;

    setLoggedInUser(nextUser);
  };

  return (
    <div>
      <header className="header">
        <h1 className="logo" onClick={()=>{nav('/')}}>지식 관리 시스템</h1>
        <nav className="nav">
          <button className="nav-button" onClick={() => {nav('/user')}}>직원</button>
          <button className="nav-button" onClick={() => {nav('/document')}}>문서</button>
          <button className="nav-button" onClick={() => {nav('/schedule')}}>일정</button>
          <button className="nav-button" onClick={() => {nav('/task')}}>업무방</button>
          <button className="nav-button" onClick={() => {nav('/education')}}>교육</button>
          <button className="nav-button" onClick={() => {nav('/club')}}>동호회</button>

        </nav>
        <div className="user-menu">
          <button className="user-button">{loggedInUser.name}</button>
          <button className="user-button"
          onClick={toggleRole}>로그아웃</button>
        </div>
      </header>
      </div>
  );
}

export default Header;