import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

interface CartItem {
  id: number
  productId: number
  skuId: number
  name: string
  image: string
  price: number
  quantity: number
  selected: boolean
}

export function createCartStore(storage?: {
  get(key: string): string | null
  set(key: string, value: string): void
  remove(key: string): void
}) {
  const KEY = 'cart_data'

  function load(): CartItem[] {
    try {
      const raw = storage?.get(KEY)
      return raw ? JSON.parse(raw) : []
    } catch { return [] }
  }

  function save(items: CartItem[]) {
    storage?.set(KEY, JSON.stringify(items))
  }

  return defineStore('cart', () => {
    const cartList = ref<CartItem[]>(load())

    if (storage) {
      watch(cartList, (v) => save(v), { deep: true })
    }

    const totalCount = computed(() => cartList.value.reduce((s, i) => s + i.quantity, 0))
    const selectedCount = computed(() => cartList.value.filter(i => i.selected).reduce((s, i) => s + i.quantity, 0))
    const totalPrice = computed(() => cartList.value.filter(i => i.selected).reduce((s, i) => s + i.price * i.quantity, 0))
    const selectedItems = computed(() => cartList.value.filter(i => i.selected))

    function addToCart(item: Partial<CartItem>) {
      const existing = cartList.value.find(i => i.productId === item.productId && i.skuId === (item.skuId || 0))
      if (existing) {
        existing.quantity += item.quantity || 1
      } else {
        cartList.value.push({
          id: Date.now(), name: '', image: '', price: 0, selected: true, skuId: 0,
          ...item,
        } as CartItem)
      }
    }

    function removeFromCart(id: number) {
      const idx = cartList.value.findIndex(i => i.id === id)
      if (idx > -1) cartList.value.splice(idx, 1)
    }

    function updateQuantity(id: number, qty: number) {
      if (qty <= 0) { removeFromCart(id); return }
      const item = cartList.value.find(i => i.id === id)
      if (item) item.quantity = qty
    }

    function toggleSelected(id: number) {
      const item = cartList.value.find(i => i.id === id)
      if (item) item.selected = !item.selected
    }

    function clearCart() { cartList.value = [] }

    return {
      cartList, totalCount, selectedCount, totalPrice, selectedItems,
      addToCart, removeFromCart, updateQuantity, toggleSelected, clearCart,
    }
  })
}

export type CartStore = ReturnType<ReturnType<typeof createCartStore>>
