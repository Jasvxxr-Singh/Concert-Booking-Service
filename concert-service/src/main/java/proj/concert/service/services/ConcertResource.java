package proj.concert.service.services;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.common.dto.UserDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.User;
import proj.concert.service.domain.Performer;
import proj.concert.service.mapper.ConcertMapper;
import proj.concert.service.config.Config;
import proj.concert.service.mapper.PerformerMapper;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/concert-service")
public class ConcertResource {
    //private static Logger LOGGER = LoggerFactory.getLogger(ConcertUtils.class);
    //private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
    PersistenceManager p = PersistenceManager.instance();
    //EntityManager em = PersistenceManager.instance().createEntityManager();


    @POST
    @Path("/login")
    public Response login(UserDTO user) {
        EntityManager em = p.createEntityManager();
        User u = null;

        try {
            em.getTransaction().begin();
            //USER has NOT YET completed!!!
            //was using list before and got an error, asking me to use typedquery
            TypedQuery<User> users = em.createQuery("select a from User a where a.username = :username and a.password = :password", User.class);
            users.setParameter("username", user.getUsername());
            users.setParameter("password", user.getPassword());
            users.setLockMode(LockModeType.PESSIMISTIC_READ);//either optimistic or pessimistic

            u = users.getResultList().stream().findFirst().orElse(null);

            if (u == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                NewCookie newCookie = new NewCookie(Config.AUTH_COOKIE, UUID.randomUUID().toString());
                u.setCookie(newCookie.getValue());
                //response = Response.ok().cookie(newCookie).build();
                em.merge(u);
                em.getTransaction().commit();
                return Response.ok().cookie(newCookie).build();
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        }


    }

    /*@GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertById(@PathParam("id") long id) {
        EntityManager em = p.createEntityManager();
        try {
            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, id);
            em.getTransaction().commit();
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(ConcertMapper.toConcertDTO(concert)).build();
        } finally {
            em.close();
        }


    }*/

    @GET // Cannot invoke "java.util.List.size()" because "concerts" is null
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<ConcertDTO> dtoconcert = new ArrayList<>();
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select a from Concert a", Concert.class);
            concertQuery.setLockMode(LockModeType.PESSIMISTIC_READ);
            List<Concert> listconcert = concertQuery.getResultList();

            em.getTransaction().commit();
            for (Concert c : listconcert) {
                dtoconcert.add(ConcertMapper.toConcertDTO(c));
            }
            return Response.ok(dtoconcert).build();
            //ist<String> list = new ArrayList<String>();
            //GenericEntity<List<String>> entity = new GenericEntity<List<String>>(list) {};
            //Response response = Response.ok(entity).build();

        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        }
    }

    @GET
    @Path("/concerts/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertSummaries() {
        EntityManager em = p.createEntityManager();
        List<ConcertSummaryDTO> concertS = new ArrayList<>();
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQ = em.createQuery("select a from Concert a", Concert.class);
            concertQ.setLockMode(LockModeType.PESSIMISTIC_READ);
            List<Concert> concerts = concertQ.getResultList();
            if (concerts.isEmpty()) {
                return Response.noContent().build();
            }
            for (Concert c : concerts) {
                concertS.add(ConcertMapper.toConcertSummaryDTO(c));
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();

        }
        return Response.ok(concertS).build();

    }

    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertById(@PathParam("id") long id) {
        EntityManager em = p.createEntityManager();
        Concert concert;

        try {
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);

            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        } return Response.ok(ConcertMapper.toConcertDTO(concert)).build();
    }

    @GET
    @Path("/performers")
    public Response getAllPerformers() {
        EntityManager em = p.createEntityManager();
        List<PerformerDTO> performerS = new ArrayList<>();
        try {
            em.getTransaction().begin();
            TypedQuery<Performer> performerQ = em.createQuery("SELECT p FROM Performer p", Performer.class);
            performerQ.setLockMode(LockModeType.PESSIMISTIC_READ);
            List<Performer> performers = performerQ.getResultList();

            em.getTransaction().commit();

            if (performers.isEmpty()) {
                return Response.noContent().build();
            }

            for (Performer p: performers) {
                performerS.add(PerformerMapper.toPerformerDTO(p));
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        }
        return Response.ok(performerS).build();
    }

    @GET
    @Path("/performers/{id}")
    public Response getPerformers(@PathParam("id") long id) {
        EntityManager em = p.createEntityManager();
        Performer performer;
        try {
            em.getTransaction().begin();
            performer = em.find(Performer.class, id);

            if (performer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            } em.close();
        } return Response.ok(PerformerMapper.toPerformerDTO(performer)).build();
    }

    // TODO Implement this.

}
