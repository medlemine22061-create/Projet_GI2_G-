package model;

public class Hospital extends MapElement {

    private boolean hasAvailableOrgan;
    private OrganType availableOrganType;

    public Hospital(
            int id,
            String name,
            Point2D position,
            boolean hasAvailableOrgan,
            OrganType availableOrganType
    ) {

        super(id, name, position);

        this.hasAvailableOrgan = hasAvailableOrgan;
        this.availableOrganType = availableOrganType;
    }

    public boolean hasAvailableOrgan() {
        return hasAvailableOrgan;
    }

    public void setHasAvailableOrgan(boolean hasAvailableOrgan) {
        this.hasAvailableOrgan = hasAvailableOrgan;
    }

    public OrganType getAvailableOrganType() {
        return availableOrganType;
    }

    public void setAvailableOrganType(OrganType availableOrganType) {
        this.availableOrganType = availableOrganType;
    }

    public boolean canProvideOrgan(OrganType requestedType) {

        return hasAvailableOrgan &&
                availableOrganType == requestedType;
    }

    @Override
    public String toString() {

        return "Hospital{" +
                "name='" + getName() + '\'' +
                ", hasAvailableOrgan=" + hasAvailableOrgan +
                ", availableOrganType=" + availableOrganType +
                '}';
    }
}
