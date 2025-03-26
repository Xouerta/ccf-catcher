import csv
from scapy.all import rdpcap
import time


class PcapDecode:
    def __init__(self):
        PROTOCOL_PATH = 'protocol'
        self.ETHER_DICT = self._read_protocol_file(f'{PROTOCOL_PATH}/ETHER')
        self.IP_DICT = self._read_protocol_file(f'{PROTOCOL_PATH}/IP')
        self.PORT_DICT = self._read_protocol_file(f'{PROTOCOL_PATH}/PORT')
        self.TCP_DICT = self._read_protocol_file(f'{PROTOCOL_PATH}/TCP')
        self.UDP_DICT = self._read_protocol_file(f'{PROTOCOL_PATH}/UDP')

    def _read_protocol_file(self, file_path):
        protocol_dict = {}
        try:
            with open(file_path, 'r', encoding='UTF-8') as f:
                lines = f.readlines()
            for line in lines:
                line = line.strip()
                parts = line.split(':')
                if len(parts) == 2:
                    try:
                        key = int(parts[0])
                        protocol_dict[key] = parts[1]
                    except ValueError:
                        print(f"无法将 {parts[0]} 转换为整数，跳过此条记录: {line}")
        except FileNotFoundError:
            print(f"未找到协议文件: {file_path}")
        return protocol_dict

    def ether_decode(self, p):
        data = {}
        if p.haslayer("Ether"):
            data = self.ip_decode(p)
            return data
        else:
            data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                         time.localtime(float(p.time)))  # 转换为float
            data['source_ip'] = ''
            data['dest_ip'] = ''
            data['protocol'] = ''
            data['len'] = len(p)
            data['info'] = p.summary()
            data['source_port'] = ''
            data['dest_port'] = ''
            return data

    def ip_decode(self, p):
        data = {}
        if p.haslayer("IP"):
            ip = p.getlayer("IP")
            if p.haslayer("TCP"):
                data = self.tcp_decode(p, ip)
                return data
            elif p.haslayer("UDP"):
                data = self.udp_decode(p, ip)
                return data
            else:
                if ip.proto in self.IP_DICT:
                    data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                                 time.localtime(float(p.time)))  # 转换为float
                    data['source_ip'] = ip.src
                    data['dest_ip'] = ip.dst
                    data['protocol'] = self.IP_DICT[ip.proto]
                    data['len'] = len(p)
                    data['info'] = p.summary()
                    data['source_port'] = 'UnKnow'
                    data['dest_port'] = 'UnKnow'
                    return data
                else:
                    data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                                 time.localtime(float(p.time)))  # 转换为float
                    data['source_ip'] = ip.src
                    data['dest_ip'] = ip.dst
                    data['protocol'] = 'IPv4'
                    data['len'] = len(p)
                    data['info'] = p.summary()
                    data['source_port'] = 'UnKnow'
                    data['dest_port'] = 'UnKnow'
                    return data
        elif p.haslayer("IPv6"):
            ipv6 = p.getlayer("IPv6")
            if p.haslayer("TCP"):
                data = self.tcp_decode(p, ipv6)
                return data
            elif p.haslayer("UDP"):
                data = self.udp_decode(p, ipv6)
                return data
            else:
                if ipv6.nh in self.IP_DICT:
                    data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                                 time.localtime(float(p.time)))  # 转换为float
                    data['source_ip'] = ipv6.src
                    data['dest_ip'] = ipv6.dst
                    data['protocol'] = self.IP_DICT[ipv6.nh]
                    data['len'] = len(p)
                    data['info'] = p.summary()
                    data['source_port'] = 'UnKnow'
                    data['dest_port'] = 'UnKnow'
                    return data
                else:
                    data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                                 time.localtime(float(p.time)))  # 转换为float
                    data['source_ip'] = ipv6.src
                    data['dest_ip'] = ipv6.dst
                    data['protocol'] = 'IPv6'
                    data['len'] = len(p)
                    data['info'] = p.summary()
                    data['source_port'] = 'UnKnow'
                    data['dest_port'] = 'UnKnow'
                    return data
        else:
            if p.type in self.ETHER_DICT:
                data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                             time.localtime(float(p.time)))  # 转换为float
                data['source_ip'] = p.src
                data['dest_ip'] = p.dst
                data['protocol'] = self.ETHER_DICT[p.type]
                data['len'] = len(p)
                data['info'] = p.summary()
                data['source_port'] = 'UnKnow'
                data['dest_port'] = 'UnKnow'
                return data
            else:
                data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                             time.localtime(float(p.time)))  # 转换为float
                data['source_ip'] = p.src
                data['dest_ip'] = p.dst
                data['protocol'] = hex(p.type)
                data['len'] = len(p)
                data['info'] = p.summary()
                data['source_port'] = 'UnKnow'
                data['dest_port'] = 'UnKnow'
                return data

    def tcp_decode(self, p, ip):
        data = {}
        tcp = p.getlayer("TCP")
        data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(float(p.time)))  # 转换为float
        data['source_ip'] = ip.src
        data['dest_ip'] = ip.dst
        data['len'] = len(p)
        data['info'] = p.summary()
        data['source_port'] = str(tcp.sport)
        data['dest_port'] = str(tcp.dport)
        if tcp.dport in self.PORT_DICT:
            data['protocol'] = self.PORT_DICT[tcp.dport]
        elif tcp.sport in self.PORT_DICT:
            data['protocol'] = self.PORT_DICT[tcp.sport]
        elif tcp.dport in self.TCP_DICT:
            data['protocol'] = self.TCP_DICT[tcp.dport]
        elif tcp.sport in self.TCP_DICT:
            data['protocol'] = self.TCP_DICT[tcp.sport]
        else:
            data['protocol'] = "TCP"
        return data

    def udp_decode(self, p, ip):
        data = {}
        udp = p.getlayer("UDP")
        data['time'] = time.strftime('%Y-%m-%d %H:%M:%S',
                                     time.localtime(float(p.time)))  # 转换为float
        data['source_ip'] = ip.src
        data['dest_ip'] = ip.dst
        data['len'] = len(p)
        data['info'] = p.summary()
        data['source_port'] = str(udp.sport)
        data['dest_port'] = str(udp.dport)
        if udp.dport in self.PORT_DICT:
            data['protocol'] = self.PORT_DICT[udp.dport]
        elif udp.sport in self.PORT_DICT:
            data['protocol'] = self.PORT_DICT[udp.sport]
        elif udp.dport in self.UDP_DICT:
            data['protocol'] = self.UDP_DICT[udp.dport]
        elif udp.sport in self.UDP_DICT:
            data['protocol'] = self.UDP_DICT[udp.sport]
        else:
            data['protocol'] = "UDP"
        return data


def save_to_csv(pcap_file, output_file):
    decoder = PcapDecode()
    packets = rdpcap(pcap_file)
    fieldnames = ['time', 'source_ip', 'dest_ip', 'protocol', 'len', 'info', 'source_port', 'dest_port']
    with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for packet in packets:
            decoded_data = decoder.ether_decode(packet)
            writer.writerow(decoded_data)


# 使用示例
pcap_file = 'test.pcap'
output_file = '../proceed_files/output.csv'
save_to_csv(pcap_file, output_file)
