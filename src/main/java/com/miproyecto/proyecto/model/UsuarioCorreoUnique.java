package com.miproyecto.proyecto.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.miproyecto.proyecto.service.EncryptionService;
import com.miproyecto.proyecto.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;


/**
 * Validate that the correo value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = UsuarioCorreoUnique.UsuarioCorreoUniqueValidator.class
)
public @interface UsuarioCorreoUnique {

    String message() default "{Exists.usuario.correo}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class UsuarioCorreoUniqueValidator implements ConstraintValidator<UsuarioCorreoUnique, String> {

        private final UsuarioService usuarioService;
        private final HttpServletRequest request;
        private final EncryptionService encryptionService;

        public UsuarioCorreoUniqueValidator(final UsuarioService usuarioService,
                final HttpServletRequest request, final EncryptionService encryptionService) {
            this.usuarioService = usuarioService;
            this.request = request;
            this.encryptionService = encryptionService;
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("idUsuario");

            if (currentId != null){
                Long idDescrypt = encryptionService.decrypt(currentId);
                
                if (value.equalsIgnoreCase(usuarioService.get(idDescrypt).getCorreo())) {
                    // value hasn't changed
                    return true;
                }      
            }
            return !usuarioService.correoExists(value);
        }

    }

}