# language: es
Característica: Creación y cancelación de pedidos

  Antecedentes:
    Dado que el usuario inicia sesión con credenciales válidas
    Y el producto "Producto E2E Base" tiene stock 20

  Escenario: Crear un pedido con descuento y cancelarlo restaurando el stock
    Cuando crea un pedido para "Cliente E2E" con 2 unidades de "Producto E2E Base"
    Entonces el pedido debe quedar en estado "Creado"
    Y el total final del pedido debe ser 114000 pesos
    Y el stock de "Producto E2E Base" debe ser 18
    Cuando cancela el pedido de "Cliente E2E"
    Entonces el pedido debe quedar en estado "Cancelado"
    Y el stock de "Producto E2E Base" debe ser 20
