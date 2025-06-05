package project;

import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@Named("yBean")
@ViewScoped
public class YBean implements Serializable {
    private Double y = 0.0;

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void validateYBeanValue(Object value) {
        if (value == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Input Y is required!", null);
            throw new ValidatorException(message);
        }

        try {
            Double yValue = Double.parseDouble(value.toString());
            if (yValue < -3.0 || yValue > 5.0) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Y must be between -3 and 5.", null);
                throw new ValidatorException(message);
            }
        } catch (NumberFormatException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Y format!", null);
            throw new ValidatorException(message);
        }
    }
}
