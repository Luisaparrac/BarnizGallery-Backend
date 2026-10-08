package com.barnizgallery.backend.patterns.adapter;

/**
 * Identifies a 3D generation task in the external service.
 * <p>
 * The table only has one column for it ({@code three_d_models.hyper3d_task_id}, varchar(150)),
 * so the ticket is stored as {@code taskUuid|subscriptionKey}. If the subscription key does not
 * fit, only the task uuid is stored and the status is checked through the download endpoint.
 *
 * @param taskUuid        id of the generation task
 * @param subscriptionKey key used to poll the status (may be null)
 */
public record GenerationTicket(String taskUuid, String subscriptionKey) {

    static final int MAX_STORED_LENGTH = 150;
    private static final String SEPARATOR = "|";

    /** Text saved in the database column. */
    public String encode() {
        if (subscriptionKey == null || subscriptionKey.isBlank()) {
            return taskUuid;
        }
        String full = taskUuid + SEPARATOR + subscriptionKey;
        return full.length() <= MAX_STORED_LENGTH ? full : taskUuid;
    }

    /** Rebuilds the ticket from the database column. */
    public static GenerationTicket decode(String stored) {
        int index = stored.indexOf(SEPARATOR);
        if (index < 0) {
            return new GenerationTicket(stored, null);
        }
        return new GenerationTicket(stored.substring(0, index), stored.substring(index + 1));
    }
}
