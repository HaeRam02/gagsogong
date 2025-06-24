import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Home from './Pages/Home';
import ClubMain from './Pages/clubs/ClubMain';
import ClubRegister from "./Pages/clubs/ClubRegister";
import EmployeeMain from './Pages/employee/EmployeeMain';
import EmployeeCreateView from './Pages/employee/EmployeeCreateView';
import EmployeeSearchView from './Pages/employee/EmployeeSearchView';
import DocuMain from './Pages/documents/DocuMain';
import EduMain from './Pages/educations/EduMain';
import ScheduleMain from './Pages/schedules/ScheduleMain';
import TaskMain from './Pages/tasks/TaskMain';
import Header from './Components/Header';
import TaskCreateView from './Pages/tasks/TaskCreateView';
import EduDetail from './Pages/educations/EduDetail';
import { UserProvider } from './Context/UserContext'; 
import RegisterDocumentView from './Pages/documents/RegisterDocumentView';
import DocumentDetailView from "./Pages/documents/DisplayDocumentDetailView";
import DisplayDocumentView from "./Pages/documents/DisplayDocumentView";

function App() {
  return (
  <UserProvider>
  <Header/>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/club" element={<ClubMain />} />
      <Route path="/club/register" element={<ClubRegister />} />
      <Route path="/document" element={<DocuMain />} />
      <Route path="/document/register" element={<RegisterDocumentView />} />
       <Route path="/document/create" element={<DisplayDocumentView />} />
        <Route path="/document/read" element={<RegisterDocumentView />} />
        <Route path="/documents/:id" element={<DocumentDetailView />} />
      <Route path="/education" element={<EduMain/>} />
      <Route path="/education/:id" element={<EduDetail />} />
      <Route path="/schedule" element={<ScheduleMain />} />
      <Route path='/task' element={<TaskMain/>}/>
      <Route path='/task/create' element={<TaskCreateView/>}/>
      <Route path='/employee/create' element={<EmployeeCreateView/>}/>
      <Route path='/employee/search' element={<EmployeeSearchView/>}/>
      <Route path="/employee" element={<EmployeeMain/>} />


    </Routes>
  </UserProvider>
  );
}

export default App;
