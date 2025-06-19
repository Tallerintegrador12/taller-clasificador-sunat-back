package upao.edu.pe.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SunatApiPropertiesTest {

    @Test
    void getUrl_debeRetornarValorAsignado() {
        SunatApiProperties props = new SunatApiProperties();
        props.setUrl("http://test-url");
        assertThat(props.getUrl()).isEqualTo("http://test-url");
    }

    @Test
    void getCookies_debeRetornarValorAsignado() {
        SunatApiProperties props = new SunatApiProperties();
        props.setCookies("cookie-test");
        assertThat(props.getCookies()).isEqualTo("cookie-test");
    }
}
