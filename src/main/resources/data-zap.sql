INSERT INTO productos (nombre, descripcion, precio, stock, estado, version) VALUES
('Cafe Demo', 'Producto ficticio para el perfil zap', 25000.00, 46, 'ACTIVO', 0),
('Cuaderno Demo', 'Producto ficticio para el perfil zap', 12000.00, 100, 'ACTIVO', 0),
('Teclado Demo', 'Producto ficticio para el perfil zap', 180000.00, 10, 'ACTIVO', 0),
('Producto Inactivo Demo', 'Producto ficticio inactivo para el perfil zap', 9000.00, 5, 'INACTIVO', 0);

INSERT INTO movimientos_inventario (producto_id, cantidad, tipo, fecha)
SELECT id, 50, 'ENTRADA', CURRENT_TIMESTAMP FROM productos WHERE nombre = 'Cafe Demo';

INSERT INTO movimientos_inventario (producto_id, cantidad, tipo, fecha)
SELECT id, 100, 'ENTRADA', CURRENT_TIMESTAMP FROM productos WHERE nombre = 'Cuaderno Demo';

INSERT INTO movimientos_inventario (producto_id, cantidad, tipo, fecha)
SELECT id, 10, 'ENTRADA', CURRENT_TIMESTAMP FROM productos WHERE nombre = 'Teclado Demo';

INSERT INTO movimientos_inventario (producto_id, cantidad, tipo, fecha)
SELECT id, 4, 'SALIDA', CURRENT_TIMESTAMP FROM productos WHERE nombre = 'Cafe Demo';

INSERT INTO pedidos (cliente, fecha, total, descuento, total_final, estado)
VALUES ('Cliente Demo', CURRENT_TIMESTAMP, 100000.00, 5000.00, 95000.00, 'CREADO');

INSERT INTO detalle_pedido (pedido_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal)
SELECT p.id, pr.id, pr.nombre, 4, pr.precio, 100000.00
FROM pedidos p, productos pr
WHERE p.cliente = 'Cliente Demo' AND pr.nombre = 'Cafe Demo';
