# language: es
Característica: Inicio de sesión en SistemaDeVentas

  Antecedentes:
    Dado que el usuario abre la página de inicio de sesión

  Escenario: Inicio de sesión con credenciales válidas
    Cuando ingresa el usuario "admin" y la contraseña "admin123"
    Entonces debe ver el panel principal
    Y debe ver el usuario autenticado en la barra superior

  Escenario: Inicio de sesión con credenciales inválidas
    Cuando ingresa el usuario "admin" y la contraseña "incorrecta"
    Entonces debe ver el mensaje "Usuario o contraseña incorrectos"
    Y debe permanecer en la página de inicio de sesión
