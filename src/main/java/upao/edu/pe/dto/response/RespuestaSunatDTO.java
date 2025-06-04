package upao.edu.pe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import upao.edu.pe.dto.MensajeSunatDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaSunatDTO {
    private String estadoRespuesta;
    private Integer page;
    private Integer startPage;
    private Integer endPage;
    private Integer total;
    private Integer records;
    private List<MensajeSunatDTO> rows;

    public List<MensajeSunatDTO> getRows() {
        return rows;
    }

    public void setRows(List<MensajeSunatDTO> rows) {
        this.rows = rows;
    }
}