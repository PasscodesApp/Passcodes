export default function formatDate(isoString: string): string {
  const date = new Date(isoString);

  const months = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "June",
    "July",
    "Augt",
    "Sept",
    "Oct",
    "Nov",
    "Dec",
  ];

  return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
}
