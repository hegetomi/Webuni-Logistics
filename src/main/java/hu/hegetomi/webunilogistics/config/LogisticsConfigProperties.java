package hu.hegetomi.webunilogistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

@ConfigurationProperties("logistics")
@Component
public class LogisticsConfigProperties {

    private final TransportDelay transportDelay = new TransportDelay();

    public TransportDelay getTransportDelay() {
        return transportDelay;
    }

    public static class TransportDelay {

        private TreeMap<Integer, Integer> penalty;

        public TreeMap<Integer, Integer> getPenalty() {
            return penalty;
        }

        public void setPenalty(TreeMap<Integer, Integer> penalty) {
            this.penalty = penalty;
        }
    }
}
