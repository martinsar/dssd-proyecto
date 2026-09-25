package dto;


public class EmergenciaContractDTO {
    private Long idEmergenciaInput;
    private String tipoDesastreInput;

    public EmergenciaContractDTO(Long idEmergenciaInput, String tipoDesastreInput) {
        this.idEmergenciaInput = idEmergenciaInput;
        this.tipoDesastreInput = tipoDesastreInput;
    }

    public Long getIdEmergenciaInput() { return idEmergenciaInput; }
    public void setIdEmergenciaInput(Long idEmergenciaInput) { this.idEmergenciaInput = idEmergenciaInput; }
    public String getTipoDesastreInput() { return tipoDesastreInput; }
    public void setTipoDesastreInput(String tipoDesastreInput) { this.tipoDesastreInput = tipoDesastreInput; }
}