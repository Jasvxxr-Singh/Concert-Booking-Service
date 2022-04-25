package proj.concert.service.services;

import antlr.collections.List;
import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


public class ConcertResource {
    PersistenceManager p = PersistenceManager.instance();
    EntityManager em = p.createEntityManager();
    @POST
    @Path('/login')
    public Response login(UserDTO user){

        try{
            em.getTransaction().begin();
            //USER has NOT YET completed!!!
            //was using list before and got an error, asking me to use typedquery
            TypedQuery<User> users = em.createQuery("select a from User a where a.username = :username and a.password = :password", User.class);
            users.setParameter("username", user.getUsername());
            users.setParameter("password", user.getPassword());
            users.setLockMode(LockModeType.OPTIMISTIC);//either optimistic or pessimistic
            em.getTransaction().commit();

        }
    }


    // TODO Implement this.

}
