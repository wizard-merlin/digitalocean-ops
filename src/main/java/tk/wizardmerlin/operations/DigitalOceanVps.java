package tk.wizardmerlin.operations;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.common.ActionStatus;
import com.myjeeva.digitalocean.common.ImageType;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DigitalOceanVps extends VpsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DigitalOceanVps.class);

    @Override
    public String changeIp(String originalIp) throws Exception {
        LOGGER.info("starting to change IP");
        DigitalOcean apiClient = new DigitalOceanClient(getAuthToken());
        List<Droplet> droplets = apiClient.getAvailableDroplets(1, 20).getDroplets();
        LOGGER.info("looking for a droplet with IP address " + originalIp);
        for (Droplet droplet : droplets) {
            String ipAddr = droplet.getNetworks().getVersion4Networks().get(0).getIpAddress();
            if (ipAddr.equals(originalIp)) {
                LOGGER.info("found a droplet with IP address " + originalIp + ", droplet ID is " + droplet.getId());
                String regionSlug = droplet.getRegion().getSlug();
                String size = droplet.getSize();
                int dropletId = droplet.getId();
                Keys keys = apiClient.getAvailableKeys(1);
                List<Key> sshKeys = keys.getKeys();
                LOGGER.info("powering off the droplet " + dropletId);
                Action powerOff = apiClient.powerOffDroplet(dropletId);
                while (apiClient.getActionInfo(powerOff.getId()).getStatus().compareTo(ActionStatus.COMPLETED) != 0) {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String snapshotName = "my_snapshot";
                LOGGER.info("taking a snapshot");
                Action takeSnapshot = apiClient.takeDropletSnapshot(dropletId, snapshotName);
                int compareResult;
                do {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    compareResult = 1;
                    try {
                        //stupid java wrapper for DigitalOcean API, throws null pointer exception randomly.
                        //Someday I'll write my own wrapper
                        compareResult = apiClient.getActionInfo(takeSnapshot.getId()).getStatus().compareTo(ActionStatus.COMPLETED);
                    } catch (java.lang.NullPointerException e) {
                        LOGGER.error("something went wrong while waiting", e);
                    }
                } while (compareResult != 0);

                List<Snapshot> snapshots = apiClient.getAvailableSnapshots(1, 10).getSnapshots();
                Snapshot snapshot = null;
                for (Snapshot item : snapshots) {
                    if (item.getName().equals(snapshotName)) {
                        snapshot = item;
                        break;
                    }
                }
                LOGGER.info("creating a new droplet from the snapshot");
                Droplet newDroplet = createDropletByImageId(sshKeys, Long.toString(snapshot.getId()), apiClient, regionSlug, size);
                int newDropletId = newDroplet.getId();

                while (apiClient.getDropletInfo(newDropletId).getNetworks().getVersion4Networks().size() == 0) {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                newDroplet = apiClient.getDropletInfo(newDropletId);
                String newIpAddr = newDroplet.getNetworks().getVersion4Networks().get(0).getIpAddress();
                LOGGER.info("the IP address of the new droplet is " + newIpAddr);
                LOGGER.info("deleting the old droplet");
                Delete delResult = apiClient.deleteDroplet(dropletId);
                LOGGER.info("delete result: " + delResult);
                LOGGER.info("deleting the snapshot");
                delResult = apiClient.deleteSnapshot(Long.toString(snapshot.getId()));
                LOGGER.info("delete result: " + delResult);
                return newIpAddr;
            }
        }
        throw new Exception("cannot find droplet with IP address " + originalIp);
    }

    private Droplet createDropletByImageId(List<Key> keys, String snapshotId, DigitalOcean apiClient, String regionSlug, String size) throws DigitalOceanException,
            RequestUnsuccessfulException {
        Droplet dropletInfo = new Droplet();
        dropletInfo.setName("api-client-test-host-byid");
        dropletInfo.setSize(size);
        dropletInfo.setRegion(new Region(regionSlug));
        Image image = new Image(snapshotId);
        image.setType(ImageType.SNAPSHOT);
        dropletInfo.setImage(image);
        dropletInfo.setEnableIpv6(Boolean.TRUE);
        dropletInfo.setInstallMonitoring(Boolean.TRUE);

        dropletInfo.setKeys(keys);

        Droplet droplet = apiClient.createDroplet(dropletInfo);
        System.out.println(droplet);
        return droplet;
    }


}
