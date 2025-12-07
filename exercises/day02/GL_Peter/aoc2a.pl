
my $totalscore = 0;

$_ = <>;
chop();
my @ranges = split(",");


for my $range ( @ranges )  {
  my ($firstId,$lastId) = ($range =~ m/(\d+)\-(\d+)/);
  my $len = length($firstId);
  my $firstPart,lastPart;
  if ($len%2) {
    $firstPart = 10**(int($len/2));
  } else {
    $firstPart = substr($firstId,0,int($len/2));
  }
  $len = length($lastId);
  if ($len%2) {
    $lastPart = 10**(int($len/2))-1;
  } else {
    $lastPart = substr($lastId,0,int($len/2));
  }
#  print "$firstId-$lastId $firstPart .. $lastPart \n";

  while ($firstPart <= $lastPart) {
    my $id = "$firstPart$firstPart";
    if ($id >= $firstId && $id <=$lastId) {
      $totalscore += $id;
#      print "$id\n";
    }
    $firstPart++;
  }
}

print "\n== $totalscore\n";
