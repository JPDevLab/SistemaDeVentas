INSERT INTO productos (nombre, descripcion, precio, stock, estado, version) VALUES
('Producto E2E Base', 'Producto ficticio para pruebas E2E', 60000.00, 20, 'ACTIVO', 0);

INSERT INTO movimientos_inventario (producto_id, cantidad, tipo, fecha)
SELECT id, 20, 'ENTRADA', CURRENT_TIMESTAMP FROM productos WHERE nombre = 'Producto E2E Base';
