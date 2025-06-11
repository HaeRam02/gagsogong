// src/App.js
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Home from './Pages/Home';
import ClubMain from './Pages/clubs/ClubMain';
import DocuMain from './Pages/documents/DocuMain';
import EduMain from './Pages/educations/EduMain';
import ScheduleMain from './Pages/schedules/ScheduleMain';
import TaskMain from './Pages/tasks/TaskMain';
import UserMain from './Pages/users/UserMain';
import Header from './Components/Header';
import TaskCreateView from './Pages/tasks/TaskCreateView';
import TaskSearchView from './Pages/tasks/TaskSearchView';
function App() {
  return (
  <>
  <Header/>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/club" element={<ClubMain />} />
      <Route path="/document" element={<DocuMain />} />
      <Route path="/education" element={<EduMain/>} />
      <Route path="/schedule" element={<ScheduleMain />} />
      <Route path='/task' element={<TaskMain/>}/>
      <Route path='/task/create' element={<TaskCreateView/>}/>
      <Route path='/task/search' element={<TaskSearchView/>}/>

      <Route path="/user" element={<UserMain />} />


    </Routes>
  </>
  );
}

export default App;
