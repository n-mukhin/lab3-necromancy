package project;

import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Named("xBean")
@ViewScoped
public class XBean implements Serializable {
    private Double x = 0.0;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void validateXBeanValue(Object value) {
        if (value == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Input X is required!", null);
            throw new ValidatorException(message);
        }

        try {
            Double xValue = Double.parseDouble(value.toString());
            if (xValue < -5.0 || xValue > 3.0) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "X must be between -5 and 3.", null);
                throw new ValidatorException(message);
            }
        } catch (NumberFormatException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid X format!", null);
            throw new ValidatorException(message);
        }
    }
}
