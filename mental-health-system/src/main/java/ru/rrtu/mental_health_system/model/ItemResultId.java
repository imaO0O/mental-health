package ru.rrtu.mental_health_system.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Составной первичный ключ для «Результата по пункту» (item_results):
 * (Номер протокола, Номер вопроса).
 */
public class ItemResultId implements Serializable {

    private Long protocol;   // protocol_number
    private Long question;   // question_number

    public ItemResultId() {}

    public ItemResultId(Long protocol, Long question) {
        this.protocol = protocol;
        this.question = question;
    }

    public Long getProtocol() { return protocol; }
    public void setProtocol(Long protocol) { this.protocol = protocol; }

    public Long getQuestion() { return question; }
    public void setQuestion(Long question) { this.question = question; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemResultId)) return false;
        ItemResultId that = (ItemResultId) o;
        return Objects.equals(protocol, that.protocol)
                && Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, question);
    }
}
