package ar.edu.unlp.dssd.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** [Entrega final] El endpoint acepta la clave de integración (X-Api-Key) que manda Bonita */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccesoIntegracion {
}
