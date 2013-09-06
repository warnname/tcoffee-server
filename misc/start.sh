cd /var/lib/tcoffee/
#sudo -u tcoffee sh -c 'source /mnt/common/etc/profile.d/vitalit_lsf.sh; export PATH=/mnt/local/bin:$PATH; /var/lib/tcoffee/play/play start ./tserver --%vital'
sudo -u tcoffee sh -c 'source /mnt/common/etc/profile.d/vitalit_lsf.sh;
                       source /etc/profile.d/modules.sh;
                       export PATH=/software/bin:$PATH;
                       module use /software/module/;
                       module add SequenceAnalysis/MultipleSequenceAlignment/T-Coffee/9.03.r1318;
                       module add SequenceAnalysis/ProtoGene/4.2.0;
                       /var/lib/tcoffee/play/play start ./tserver --%vital'
cd - >/dev/null
