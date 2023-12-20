package tools;

import java.io.Serializable;

@lombok.extern.slf4j.Slf4j
public class Triple<E, F, G> implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public E left;
    public F mid;
    public G right;

    public Triple(E left, F mid, G right) {
        this.left = left;
        this.mid = mid;
        this.right = right;
    }

    public E getLeft() {
        return left;
    }

    public F getMid() {
        return mid;
    }

    public G getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Left: " + left.toString() + " Mid: " + mid.toString() + " Right:" + right.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((mid == null) ? 0 : mid.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Triple<E, F, G> other = (Triple<E, F, G>) obj;
        if (left == null) {
            if (other.left != null) {
                return false;
            }
        } else if (!left.equals(other.left)) {
            return false;
        }
        if (mid == null) {
            if (other.mid != null) {
                return false;
            }
        } else if (!mid.equals(other.mid)) {
            return false;
        }
        if (right == null) {
            return other.right == null;
        } else return right.equals(other.right);
    }
}
