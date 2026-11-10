# language: es
Característica: Gestión de productos e inventario

  Antecedentes:
    Dado que el usuario inicia sesión con credenciales válidas

  Escenario: Crear un producto y registrar una entrada de inventario
    Cuando crea un producto E2E con precio 50000 y stock 10
    Entonces el producto E2E debe aparecer en el catálogo con stock 10
    Cuando registra una entrada de 5 unidades para el producto E2E
    Entonces el stock del producto E2E debe ser 15
    Y debe existir un movimiento de entrada de 5 unidades para el producto E2E
