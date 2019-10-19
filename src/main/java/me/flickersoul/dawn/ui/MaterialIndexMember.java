package me.flickersoul.dawn.ui;

public class MaterialIndexMember {
    private int serial;
    private String material_name;
    private String description;
    private boolean isShown;

    public MaterialIndexMember(int serial, String material_name, String description, boolean sign){
        this.serial = serial;
        this.material_name = material_name;
        this.description = description;
        this.isShown = sign;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public String getMaterial_name() {
        return material_name;
    }

    public void setMaterial_name(String material_name) {
        this.material_name = material_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isShown() {
        return isShown;
    }

    public void setShown(boolean shown) {
        isShown = shown;
    }

    @Override
    public String toString(){
        return "serial: " + serial + ", material name: \"" + material_name + "\", description: " + description;
    }
}
