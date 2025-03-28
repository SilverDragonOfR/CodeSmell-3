@Entity
@Table(name = JPAURelationship.TABLE, uniqueConstraints =
        @UniqueConstraint(columnNames = { "type_id", "user_id", "anyObject_id" }))
public class JPAURelationship extends AbstractGeneratedKeyEntity implements URelationship {

    private static final long serialVersionUID = 2778494939240083204L;

    public static final String TABLE = "URelationship";

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private JPARelationshipType type;

    private User leftEnd;

    private AnyObject rightEnd;

    @Override
    public RelationshipType getType() {
        return type;
    }

    @Override
    public void setType(final RelationshipType type) {
        if (MembershipType.getInstance().getKey().equalsIgnoreCase(type.getKey())) {
            throw new IllegalArgumentException("This is not a membership");
        }
        checkType(type, JPARelationshipType.class);
        this.type = (JPARelationshipType) type;
    }

    @Override
    public User getLeftEnd() {
        return leftEnd;
    }

    @Override
    public void setLeftEnd(final User leftEnd) {
        checkType(leftEnd, JPAUser.class);
        this.leftEnd = (JPAUser) leftEnd;
    }

    @Override
    public AnyObject getRightEnd() {
        return rightEnd;
    }

    @Override
    public void setRightEnd(final AnyObject rightEnd) {
        checkType(rightEnd, JPAAnyObject.class);
        this.rightEnd = (JPAAnyObject) rightEnd;
    }
}