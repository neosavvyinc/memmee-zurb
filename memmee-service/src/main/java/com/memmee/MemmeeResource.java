package com.memmee;


import java.util.Date;
import java.util.List;

import com.memmee.domain.attachment.dao.TransactionalAttachmentDAO;
import com.memmee.domain.attachment.dto.Attachment;
import com.memmee.domain.inspirations.dao.TransactionalInspirationDAO;
import com.memmee.domain.memmees.dao.TransactionalMemmeeDAO;
import com.memmee.domain.memmees.dto.Memmee;
import com.memmee.domain.user.dao.TransactionalUserDAO;
import com.memmee.domain.user.dto.User;
import com.memmee.theme.dao.TransactionalThemeDAO;
import com.memmee.theme.dto.Theme;
import com.memmee.util.ListUtil;
import com.memmee.util.OsUtil;
import com.yammer.dropwizard.logging.Log;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.TransactionStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;


@Path("/memmeerest")
public class MemmeeResource {
    public static final String BASE_URL = "memmeerest";

    private final TransactionalUserDAO userDao;
    private final TransactionalMemmeeDAO memmeeDao;
    private final TransactionalAttachmentDAO attachmentDAO;
    private final TransactionalInspirationDAO inspirationDAO;
    private final TransactionalThemeDAO themeDAO;
    private static final Log LOG = Log.forClass(MemmeeResource.class);

    public MemmeeResource(TransactionalUserDAO userDao, TransactionalMemmeeDAO memmeeDao, TransactionalAttachmentDAO attachmentDao, TransactionalInspirationDAO inspirationDao, TransactionalThemeDAO themeDao) {
        super();
        this.userDao = userDao;
        this.memmeeDao = memmeeDao;
        this.attachmentDAO = attachmentDao;
        this.inspirationDAO = inspirationDao;
        this.themeDAO = themeDao;
    }


    @GET
    @Path("/getmemmees")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Memmee> getMemmees(@QueryParam("apiKey") String apiKey) {

        User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        List<Memmee> memmeesbyUser = memmeeDao.getMemmeesbyUser(user.getId());

        return memmeesbyUser;
    }


    @GET
    @Path("/getmemmee")
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee getMemmee(@QueryParam("apiKey") String apiKey, @QueryParam("id") Long id) {

        User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        if (id == null) {
            List<Memmee> list = memmeeDao.getMemmeesbyUser(user.getId());

            if (!ListUtil.nullOrEmpty(list))
                return list.get(0);
            return new Memmee(user.getId(), Memmee.NO_MEMMEES_TEXT);
        }
        return memmeeDao.getMemmee(id);
    }

    @POST
    @Path("/insertmemmee")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee insertMemmee(@QueryParam("apiKey") String apiKey, @Valid final Memmee memmee) {

        long memmeeId = -1;

        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        try {
            final Attachment attachment = memmee.getAttachment();
            final Long inspirationId = memmee.getInspiration() != null ? memmee.getInspiration().getId() : null;
            final Long themeId = getOrCreateMemmeeTheme(memmee.getTheme());
            if (attachment != null) {

                memmeeId = memmeeDao.inTransaction(new Transaction<Integer, TransactionalMemmeeDAO>() {
                    public Integer inTransaction(TransactionalMemmeeDAO tx, TransactionStatus status) throws Exception {

                        Date timeOfInsert = new Date();

                        if (memmee.getCreationDate() == null) {
                            LOG.error("Memmee is being inserted with a null creation date");
                            memmee.setCreationDate(timeOfInsert);
                        }

                        if (memmee.getDisplayDate() == null) {
                            LOG.error("Memmee is being inserted with a null display date");
                            memmee.setDisplayDate(timeOfInsert);
                        }

                        //Save the hard returns as <br> tags
                        memmee.setText(memmee.getText().replaceAll("(\r\n|\n)", "<br />"));
                        Long memmeeId = memmeeDao.insert(user.getId(), memmee.getText(),
                                timeOfInsert, memmee.getCreationDate(), memmee.getDisplayDate(), "", null, themeId, inspirationId);

                        Long attachmentId;

                        memmeeDao.updateAttachment(attachment.getId(), attachment.getFilePath(), attachment.getType());

                        attachmentId = attachment.getId();

                        memmeeDao.update(memmeeId, memmee.getText(), new Date(), memmee.getDisplayDate(), null, attachmentId, themeId);

                        return memmeeId.intValue();
                    }
                });
            } else {
                memmee.setText(memmee.getText().replaceAll("(\r\n|\n)", "<br />"));
                memmeeId = memmeeDao.insert(
                        user.getId()
                        , memmee.getText()
                        , new Date()
                        , new Date()
                        , memmee.getDisplayDate()
                        , memmee.getShareKey()
                        , null
                        , themeId
                        , inspirationId).intValue();
            }

        } catch (DBIException dbException) {
            LOG.error("DB EXCEPTION", dbException);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }


        return memmeeDao.getMemmee(new Long(memmeeId));

    }

    /**
     * Potentially this method can be removed as I think the method above satisifies
     * the needs that were intended below
     */
    @PUT
    @Path("/updatememmeewithattachment")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee updateMemmeeWithAttachment(@QueryParam("apiKey") String apiKey, @Valid final Memmee memmee) {

        int count = 0;

        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        try {

            final Attachment attachment = memmee.getAttachment();
            final Long themeId = memmee.getTheme() != null ? memmee.getTheme().getId() : null;
            count = memmeeDao.inTransaction(new Transaction<Integer, TransactionalMemmeeDAO>() {
                public Integer inTransaction(TransactionalMemmeeDAO tx, TransactionStatus status) throws Exception {

                    int count = memmeeDao.update(memmee.getId(), memmee.getText(),
                            new Date(), new Date(), memmee.getShareKey(), attachment.getId(), themeId);

                    memmeeDao.updateAttachment(attachment.getId(), attachment.getFilePath(), attachment.getType());

                    return count;

                }
            });

        } catch (DBIException dbException) {
            LOG.error("DB EXCEPTION", dbException);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        if (count == 0) {
            LOG.error("MEMMEE NOT UPDATED");
            throw new WebApplicationException(Status.NOT_MODIFIED);
        }

        Memmee returnValue = memmeeDao.getMemmee(new Long(memmee.getId()));

        return returnValue;

    }

    @PUT
    @Path("/updatememmee")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee updateMemmee(@QueryParam("apiKey") String apiKey, @Valid final Memmee memmee) {

        int count = 0;

        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        try {


            count = memmeeDao.update(memmee.getId(), memmee.getText(),
                    new Date(), new Date(), memmee.getShareKey(), memmee.getAttachment().getId(), memmee.getTheme().getId());

        } catch (DBIException dbException) {
            LOG.error("DB EXCEPTION", dbException);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        if (count == 0) {
            LOG.error("MEMMEE NOT UPDATED");
            throw new WebApplicationException(Status.NOT_MODIFIED);
        }

        return memmeeDao.getMemmee(new Long(memmee.getId()));

    }


    @PUT
    @Path("/sharememmee")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee shareMemmee(@QueryParam("apiKey") String apiKey, @Valid final Memmee memmee) {

        int count = 0;

        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        try {

            String shareKey = (UUID.randomUUID().toString());
            count = memmeeDao.update(
                    memmee.getId()
                    , memmee.getText()
                    , new Date()                     //Last Update Date is updated to server supplied
                    , memmee.getDisplayDate()        //Updating Display Date to user supplied value
                    , shareKey
                    , memmee.getAttachment().getId()
                    , memmee.getTheme().getId());

        } catch (DBIException dbException) {
            LOG.error("DB EXCEPTION", dbException);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        if (count == 0) {
            LOG.error("MEMMEE NOT UPDATED");
            throw new WebApplicationException(Status.NOT_MODIFIED);
        }

        return memmeeDao.getMemmee(new Long(memmee.getId()));

    }

    @GET
    @Path("/open")
    @Produces({MediaType.APPLICATION_JSON})
    public Memmee openShare(@QueryParam("shareKey") String shareKey) {

        try {

            Memmee memmee = memmeeDao.getMemmee(shareKey);
            if (memmee == null) {
                throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            }
            return memmee;

        } catch (DBIException dbException) {
            LOG.error("DB EXCEPTION", dbException);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

    }


    @DELETE
    @Path("/deletememmee")
    @Produces({MediaType.APPLICATION_JSON})
    public void delete(@QueryParam("apiKey") String apiKey, @QueryParam("id") final Long id) {
        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        memmeeDao.inTransaction(new Transaction<Long, TransactionalMemmeeDAO>() {
            public Long inTransaction(TransactionalMemmeeDAO tx, TransactionStatus status) throws Exception {
                Memmee memmee = memmeeDao.getMemmee(id);
                if (memmee != null && memmee.getAttachment() != null) {
                    memmeeDao.deleteAttachment(memmee.getAttachment().getId());
                }
                memmeeDao.delete(id);

                return id;
            }
        });

        LOG.info("Memmee with id: " + id + " was successfully deleted");
    }


    @DELETE
    @Path("/deleteattachment")
    @Produces({MediaType.APPLICATION_JSON})
    public void deleteAttachment(@QueryParam("apiKey") String apiKey, @QueryParam("id") final Long id) {
        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        memmeeDao.deleteAttachment(id);

        LOG.info("Attachment with id: " + id + " was successfully deleted");

    }

    @POST
    @Path("/uploadattachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Attachment uploadFile(
            @QueryParam("apiKey") String apiKey,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {


        final User user = userDao.getUserByApiKey(apiKey);

        if (user == null) {
            LOG.error("USER NOT FOUND FOR API KEY:" + apiKey);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        String baseFileDirectory = "";
        String uploadedFileLocation = "";
        final Attachment attachment;

        try {


            if (OsUtil.isWindows()) {
                baseFileDirectory = "c://memmee/temp/" + user.getId() + "/";
            } else if (OsUtil.isMac()) {
                baseFileDirectory = "/memmee/" + user.getId() + "/";
            } else if (OsUtil.isUnix()) {
                baseFileDirectory = "/memmee/" + user.getId() + "/";
            }
            ensureParentDirectory(baseFileDirectory);

            uploadedFileLocation = fileDetail.getFileName().toLowerCase();

            // save it
            String uploadedFileLocationToWrite = baseFileDirectory + uploadedFileLocation;
            writeToFile(uploadedInputStream, uploadedFileLocationToWrite);
            String uploadedThumbFileLocation = writeThumbnailImage(uploadedFileLocationToWrite);
            Long attachmentId = attachmentDAO.insert(uploadedFileLocationToWrite, uploadedThumbFileLocation, "Image");
            attachment = attachmentDAO.getAttachment(attachmentId);

        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()) {
                LOG.error("The user attempted to upload a file that isn't supported ");
            } else {
                LOG.error("Unhandled exception occurred: " + e.getResponse().getStatus());
            }
            LOG.error("ERROR UPLOADING ATTACHMENT ");

            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.error("ERROR UPLOADING ATTACHMENT FOR UNKNOWN REASON");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        return attachment;
    }

    //@TODO, this may later be replaced by a more robust process where all theems are fed from the backend
    protected Long getOrCreateMemmeeTheme(Theme theme) {
        Long themeId = null;

        if (theme != null) {
            if (theme.getId() != null && theme.getId() > 0)
                themeId = theme.getId();
            else {
                try {
                    themeId = themeDAO.getThemeByName(theme.getName()).getId();
                } catch (Error error) {
                    themeId = themeDAO.insert(theme.getName(), theme.getStylePath());
                }
            }
        }

        return themeId;
    }

    private void ensureParentDirectory(String parentDirectory) {
        File parentDir;
        if (parentDirectory != null) {
            parentDir = new File(parentDirectory);
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        } else {
            throw new WebApplicationException(Status.PRECONDITION_FAILED);
        }
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {
        try {
            OutputStream out;
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String writeThumbnailImage(String fileName) {
        String imPath = "/opt/local/bin:/usr/bin:/usr/local/bin";
        ConvertCmd
                cmd = new ConvertCmd();
        cmd.setSearchPath(imPath);

        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        String sourceImage = fileName;
        String destinationImage;
        if (sourceImage.toLowerCase().indexOf(".jpg") > -1) {
            destinationImage = sourceImage.replaceFirst(".jpg", "-thumb.jpg");
        } else if (sourceImage.toLowerCase().indexOf(".jpeg") > -1) {
            destinationImage = sourceImage.replaceFirst(".png", "-thumb.jpeg");
        } else if (sourceImage.toLowerCase().indexOf(".png") > -1) {
            destinationImage = sourceImage.replaceFirst(".png", "-thumb.png");
        } else {
            WebApplicationException unsupportedMediaException = new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
            LOG.error("Attempting to save a file named " + sourceImage.toLowerCase());
            throw unsupportedMediaException;
        }


        op.addImage(sourceImage);
        op.resize(200, 300);
        op.addImage(destinationImage);

        try {
            cmd.run(op);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IM4JavaException e) {
            e.printStackTrace();
        }

        return destinationImage;
    }


}
