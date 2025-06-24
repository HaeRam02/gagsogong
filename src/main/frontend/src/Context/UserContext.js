import React, { createContext, useState } from 'react';

const dummyUsers = {
  admin: {
    id: 'ADMIN_001',
    name: '김관리자',
    role: 'ADMIN'
    
  },
  employee: {
    id: 'EMP_007',
    name: '홍길동',
    deptId: 'DEV001',
    role: 'EMPLOYEE'
  }
};
console.log("초기 employee 확인:", dummyUsers.employee);

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [loggedInUser, setLoggedInUser] = useState(dummyUsers.employee); 

  return (
    <UserContext.Provider value={{ loggedInUser, setLoggedInUser, dummyUsers }}>
      {children}
    </UserContext.Provider>
  );
};
