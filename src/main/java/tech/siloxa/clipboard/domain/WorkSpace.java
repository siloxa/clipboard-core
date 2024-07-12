package tech.siloxa.clipboard.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A WorkSpace.
 */
@Entity
@Table(name = "work_space")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WorkSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "workSpace")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "workSpace" }, allowSetters = true)
    private Set<ClipBoard> clipBoards = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "rel_work_space__user",
        joinColumns = @JoinColumn(name = "work_space_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<User> users = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public WorkSpace id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public WorkSpace name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public WorkSpace user(User user) {
        this.setUser(user);
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<ClipBoard> getClipBoards() {
        return this.clipBoards;
    }

    public void setClipBoards(Set<ClipBoard> clipBoards) {
        if (this.clipBoards != null) {
            this.clipBoards.forEach(i -> i.setWorkSpace(null));
        }
        if (clipBoards != null) {
            clipBoards.forEach(i -> i.setWorkSpace(this));
        }
        this.clipBoards = clipBoards;
    }

    public WorkSpace clipBoards(Set<ClipBoard> clipBoards) {
        this.setClipBoards(clipBoards);
        return this;
    }

    public WorkSpace addClipBoard(ClipBoard clipBoard) {
        this.clipBoards.add(clipBoard);
        clipBoard.setWorkSpace(this);
        return this;
    }

    public WorkSpace removeClipBoard(ClipBoard clipBoard) {
        this.clipBoards.remove(clipBoard);
        clipBoard.setWorkSpace(null);
        return this;
    }

    public Set<User> getUsers() {
        return this.users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public WorkSpace users(Set<User> users) {
        this.setUsers(users);
        return this;
    }

    public WorkSpace addUser(User user) {
        this.users.add(user);
        return this;
    }

    public WorkSpace removeUser(User user) {
        this.users.remove(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkSpace)) {
            return false;
        }
        return id != null && id.equals(((WorkSpace) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WorkSpace{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
