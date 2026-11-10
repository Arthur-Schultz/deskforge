export type ArtKind =
  | 'KEYBOARD'
  | 'MOUSE'
  | 'HEADSET'
  | 'DESKMAT'
  | 'MICROPHONE'
  | 'WEBCAM'
  | 'LIGHTBAR'
  | 'MONITOR_ARM'

export type Role = 'CUSTOMER' | 'ADMIN'

export type OrderStatus = 'AWAITING_PAYMENT' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELED'

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Category {
  id: number
  slug: string
  name: string
  description: string
}

export interface ProductSummary {
  id: number
  sku: string
  slug: string
  name: string
  shortDescription: string
  price: number
  compareAtPrice?: number
  stock: number
  art: ArtKind
  accentColor: string
  featured: boolean
  active: boolean
  categorySlug: string
  categoryName: string
}

export interface ProductDetail extends Omit<ProductSummary, 'categorySlug' | 'categoryName'> {
  description: string
  category: Category
  specs: { label: string; value: string }[]
  related: ProductSummary[]
}

export interface User {
  id: string
  fullName: string
  email: string
  role: Role
  marketingConsent: boolean
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
  user: User
}

export interface Address {
  recipientName: string
  zipCode: string
  street: string
  number: string
  complement?: string
  district: string
  city: string
  state: string
}

export interface OrderItem {
  productId: number
  productName: string
  productSku: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order {
  id: string
  number: string
  status: OrderStatus
  items: OrderItem[]
  subtotal: number
  shippingCost: number
  total: number
  approximateTaxes: number
  shippingAddress: Address
  cancelReason?: string
  createdAt: string
  paidAt?: string
  shippedAt?: string
  deliveredAt?: string
  canceledAt?: string
  expiresAt: string
  withdrawalDeadline?: string
  cancelable: boolean
}

export interface AdminOrderSummary {
  id: string
  number: string
  customerName: string
  customerEmail: string
  status: OrderStatus
  nextStatuses: OrderStatus[]
  total: number
  city: string
  state: string
  createdAt: string
}

export interface Dashboard {
  revenue: number
  customers: number
  openPrivacyRequests: number
  ordersByStatus: Record<OrderStatus, number>
  lowStock: ProductSummary[]
}

export interface StoreInfo {
  tradeName: string
  legalName: string
  cnpj: string
  address: string
  supportEmail: string
  supportPhone: string
  supportHours: string
  dataProtectionOfficer: string
  dataProtectionEmail: string
  privacyPolicyVersion: string
  withdrawalDays: number
  supportResponseDays: number
  dataSubjectResponseDays: number
  paymentWindowMinutes: number
  freeShippingThreshold: number
  flatShippingCost: number
  approximateTaxRate: number
  demoMode: boolean
}

export interface Protocol {
  protocol: string
  createdAt: string
  respondBy: string
}

export type DataSubjectRequestType =
  | 'CONFIRMATION'
  | 'ACCESS'
  | 'CORRECTION'
  | 'ANONYMIZATION'
  | 'PORTABILITY'
  | 'DELETION'
  | 'SHARING_INFO'
  | 'CONSENT_REVOCATION'
