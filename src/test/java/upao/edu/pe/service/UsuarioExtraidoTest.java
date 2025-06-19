package upao.edu.pe.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioExtraidoTest {
    @Test
    void testConstructorVacioYSetters() {
        SunatServicio.UsuarioExtraido usuario = new SunatServicio.UsuarioExtraido();
        usuario.setRuc("12345678901");
        usuario.setUsuario("TESTUSER");
        assertEquals("12345678901", usuario.getRuc());
        assertEquals("TESTUSER", usuario.getUsuario());
    }

    @Test
    void testConstructorConParametrosYGetters() {
        SunatServicio.UsuarioExtraido usuario = new SunatServicio.UsuarioExtraido("10987654321", "USER2");
        assertEquals("10987654321", usuario.getRuc());
        assertEquals("USER2", usuario.getUsuario());
    }
}
