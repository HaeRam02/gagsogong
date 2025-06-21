// src/App.js
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Home from './Pages/Home';
import ClubMain from './Pages/clubs/ClubMain';
mport ClubRegister from "./Pages/clubs/ClubRegister";
import DocuMain from './Pages/documents/DocuMain';
import EduMain from './Pages/educations/EduMain';
import ScheduleMain from './Pages/schedules/ScheduleMain';
import TaskMain from './Pages/tasks/TaskMain';
import UserMain from './Pages/users/UserMain';
import EmployeeMain from './Pages/employees/EmployeeMain';
import EmployeeCreateView from './Pages/employees/EmployeeCreateView';
import EmployeeSearchView from './Pages/employees/EmployeeSearchView';
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
      <Route path="/club/register" element={<ClubRegister />} />
      <Route path="/document" element={<DocuMain />} />
      <Route path="/education" element={<EduMain/>} />
      <Route path="/schedule" element={<ScheduleMain />} />
      <Route path='/task' element={<TaskCreateView/>}/>
      <Route path='/task' element={<TaskMain/>}/>
      <Route path='/task/create' element={<TaskCreateView/>}/>
      <Route path='/task/search' element={<TaskSearchView/>}/>
      <Route path='/employee/create' element={<EmployeeCreateView/>}/>
      <Route path='/employee/search' element={<EmployeeSearchView/>}/>

      <Route path="/employee" element={<EmployeeMain/>} />
      <Route path="/user" element={<UserMain />} />


    </Routes>
  </>
  );
}

export default App;
