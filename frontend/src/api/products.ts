import type { Product } from "../domain/product";

export interface ProductsPage {
  items: Product[];
  totalItems: number;
  page: number;
  size: number;
}

const BASE_URL = import.meta.env.VITE_API_URL ?? "";

export async function getProducts(page = 0, size = 20): Promise<ProductsPage> {
  const response = await fetch(`${BASE_URL}/products?page=${page}&size=${size}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch products: ${response.status}`);
  }
  return response.json();
}

export async function getProductById(id: string): Promise<Product> {
  const response = await fetch(`${BASE_URL}/products/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch product: ${response.status}`);
  }
  return response.json();
}
