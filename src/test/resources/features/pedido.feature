# language: es
Característica: Descuentos y cancelación de pedidos

  Escenario: Compra desde $100.000 aplica descuento del 5 por ciento
    Dado un pedido con compra total de 100000 pesos
    Cuando se calcula el porcentaje de descuento
    Entonces el descuento debe ser del 5 por ciento
    Y el total final debe ser 95000 pesos

  Escenario: Un pedido entregado no puede cancelarse
    Dado un pedido entregado
    Cuando se intenta cancelar el pedido
    Entonces el sistema debe indicar que un pedido entregado no puede cancelarse