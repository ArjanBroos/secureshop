import { useEffect, useState } from "react";
import { getProducts, type ProductsPage } from "../../api/products";

export function CatalogPage() {
  const [page, setPage] = useState<ProductsPage | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    getProducts()
      .then(setPage)
      .catch(() => setError(true));
  }, []);

  if (error) return <p>Failed to load products. Please try again later.</p>;
  if (!page) return <p>Loading...</p>;

  return (
    <main>
      <h1>Products</h1>
      <ul>
        {page.items.map((product) => (
          <li key={product.id}>
            <strong>{product.name}</strong> — {product.description}
          </li>
        ))}
      </ul>
    </main>
  );
}
