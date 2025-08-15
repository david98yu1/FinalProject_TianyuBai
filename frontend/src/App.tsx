import { Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import SignUp from './pages/SignUp'
import ItemsPage from './pages/ItemsPage'
import AccountPage from './pages/AccountPage'
import OrderDetailPage from './pages/OrderDetailPage'
import OrdersPage from './pages/OrdersPage'
import { useAuth } from './context/AuthContext'
import ItemEditor from "./pages/ItemEditor.tsx";

function TopNav() {
  const { user, logout } = useAuth()
  const nav = useNavigate()

  return (
    <header style={{display:'flex', gap:16, padding:'12px 16px', borderBottom:'1px solid #eee', alignItems:'center'}}>
      <Link to="/" style={{fontWeight:700}}>Shop</Link>
      <nav style={{display:'flex', gap:12}}>
        <NavLink to="/items">Items</NavLink>
        {user && <NavLink to="/orders">Orders</NavLink>}
      </nav>
      <div style={{marginLeft:'auto', display:'flex', gap:12, alignItems:'center'}}>
        {user ? (
          <>
            <NavLink to="/account">Hi, {user.username ?? user.email ?? 'you'}</NavLink>
            <button onClick={() => { logout(); nav('/login'); }}>Logout</button>
          </>
        ) : (
          <>
            <NavLink to="/login">Login</NavLink>
            <NavLink to="/signup">Sign up</NavLink>
          </>
        )}
      </div>
    </header>
  )
}

export default function App() {
  return (
    <div>
      <TopNav />
      <main>
        <Routes>
          <Route path="/" element={<ItemsPage />} />
          <Route path="/items" element={<ItemsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/account" element={<AccountPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:id" element={<OrderDetailPage />} />
          <Route path="/admin/items/new" element={<ItemEditor />} />
          <Route path="/admin/items/edit" element={<ItemEditor />} />
        </Routes>
      </main>
    </div>
  )
}