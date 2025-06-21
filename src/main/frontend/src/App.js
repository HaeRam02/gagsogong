// src/App.js
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Home from './Pages/Home';
import ClubMain from './Pages/clubs/ClubMain';
import ClubRegister from "./Pages/clubs/ClubRegister";
import DocuMain from './Pages/documents/DocuMain';
import EduMain from './Pages/educations/EduMain';
import ScheduleMain from './Pages/schedules/ScheduleMain';
import TaskMain from './Pages/tasks/TaskMain';
import UserMain from './Pages/users/UserMain';
import Header from './Components/Header';
import TaskCreateView from './Pages/tasks/TaskCreateView';
function App() {
  return (
  <>
  <Header/>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/club" element={<ClubMain />} />
      <Route path="/club/register" element={<ClubRegister />} />
      <Route path="/document" element={<DocuMain />} />
      <Route path="/education" element={<EduMain/>} />
      <Route path="/schedule" element={<ScheduleMain />} />
      <Route path='/task' element={<TaskCreateView/>}/>
      <Route path="/user" element={<UserMain />} />


    </Routes>
  </>
  );
}

export default App;
