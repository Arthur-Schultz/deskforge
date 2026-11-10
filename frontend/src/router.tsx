import { createBrowserRouter } from 'react-router'
import { Layout } from './components/Layout'
import { RequireAuth } from './components/RequireAuth'
import { CartPage } from './pages/CartPage'
import { CatalogPage } from './pages/CatalogPage'
import { CheckoutPage } from './pages/CheckoutPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ProductPage } from './pages/ProductPage'
import { RegisterPage } from './pages/RegisterPage'
import { AccountPage } from './pages/account/AccountPage'
import { OrderDetailPage } from './pages/account/OrderDetailPage'
import { OrdersPage } from './pages/account/OrdersPage'
import { AdminLayout } from './pages/admin/AdminLayout'
import { AdminOrdersPage } from './pages/admin/AdminOrdersPage'
import { AdminProductsPage } from './pages/admin/AdminProductsPage'
import { DashboardPage } from './pages/admin/DashboardPage'
import { AccessibilityPage } from './pages/legal/AccessibilityPage'
import { ContactPage } from './pages/legal/ContactPage'
import { CookiesPage } from './pages/legal/CookiesPage'
import { CreditsPage } from './pages/legal/CreditsPage'
import { DataRequestPage } from './pages/legal/DataRequestPage'
import { PrivacyPage } from './pages/legal/PrivacyPage'
import { RefundPage } from './pages/legal/RefundPage'
import { TermsPage } from './pages/legal/TermsPage'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'produtos', element: <CatalogPage /> },
      { path: 'produtos/:slug', element: <ProductPage /> },
      { path: 'carrinho', element: <CartPage /> },
      { path: 'checkout', element: <RequireAuth><CheckoutPage /></RequireAuth> },
      { path: 'entrar', element: <LoginPage /> },
      { path: 'criar-conta', element: <RegisterPage /> },
      { path: 'conta', element: <RequireAuth><AccountPage /></RequireAuth> },
      { path: 'conta/pedidos', element: <RequireAuth><OrdersPage /></RequireAuth> },
      { path: 'conta/pedidos/:id', element: <RequireAuth><OrderDetailPage /></RequireAuth> },
      {
        path: 'admin',
        element: <RequireAuth role="ADMIN"><AdminLayout /></RequireAuth>,
        children: [
          { index: true, element: <DashboardPage /> },
          { path: 'pedidos', element: <AdminOrdersPage /> },
          { path: 'produtos', element: <AdminProductsPage /> },
        ],
      },
      { path: 'privacidade', element: <PrivacyPage /> },
      { path: 'termos', element: <TermsPage /> },
      { path: 'cookies', element: <CookiesPage /> },
      { path: 'reembolso', element: <RefundPage /> },
      { path: 'lgpd', element: <DataRequestPage /> },
      { path: 'contato', element: <ContactPage /> },
      { path: 'acessibilidade', element: <AccessibilityPage /> },
      { path: 'creditos', element: <CreditsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
